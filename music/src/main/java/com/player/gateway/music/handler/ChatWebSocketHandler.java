package com.player.gateway.music.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.gateway.common.entity.ChatEntity;
import com.player.gateway.music.constants.SystemtConstants;
import com.player.gateway.music.entity.ChatParamsEntity;
import com.player.gateway.music.mapper.ChatMapper;
import com.player.gateway.common.utils.JwtToken;
import com.player.gateway.music.tools.MusicTool;
import com.player.gateway.music.uitls.ChatUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    @Qualifier("qwenChatClient")
    private ChatClient qwenChatClient;

    @Autowired
    @Qualifier("deepseekChatClient")
    private ChatClient deepseekChatClient;

    private final ChatMapper chatMapper;
    @Autowired
    private VectorStore vectorStore;

    public ChatWebSocketHandler(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    @Value("${token.secret}")
    private String secret;

    @Autowired
    private MusicTool musicTool;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            String token = (String) payload.get("token");
            String userId = JwtToken.getId(token, secret);
            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            String modelName = (String) payload.get("modelName");
            String type = (String) payload.get("type");
            boolean showThink = (boolean) payload.get("showThink");
            String language = (String) payload.get("language");

            ChatParamsEntity chatParamsEntity = new ChatParamsEntity();
            chatParamsEntity.setChatId(chatId);
            chatParamsEntity.setModelName(modelName);
            chatParamsEntity.setPrompt(prompt);
            chatParamsEntity.setShowThink(showThink);
            chatParamsEntity.setLanguage(language);
            chatParamsEntity.setType(type);

            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModelName(modelName);

            ChatClient chatClient = getChatClientByModelName(modelName);

            if (chatClient == null) {
                session.sendMessage(new TextMessage("{\"error\": \"Unsupported model: " + modelName + "\"}"));
                return;
            }

            Flux<String> chatStream  = ChatUtils.processChat(
                    chatParamsEntity,
                    chatClient,
                    vectorStore,
                    userId,
                    SystemtConstants.MUSIC_SYSTEMT_PROMPT,
                    musicTool  // Added musicTool parameter
            );

            chatStream.subscribe(
                    responsePart -> {
                        chatEntity.setContent(chatEntity.getContent() + responsePart);
                        sendResponse(session, responsePart);
                    },
                    throwable -> {
                        log.error("Error during streaming", throwable);
                        try {
                            session.sendMessage(new TextMessage("{\"error\": \"AI响应异常中断\"}"));
                        } catch (IOException e) {
                            log.error("Failed to send error message", e);
                        }
                    },
                    () -> {
                        chatMapper.saveChat(chatEntity);
                        try {
                            session.sendMessage(new TextMessage("[completed]"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            try {
                session.sendMessage(new TextMessage("{\"error\": \"" + e.getMessage() + "\"}"));
            } catch (IOException ex) {
                log.error("Failed to send error message", ex);
            }
        }
    }

    private ChatClient getChatClientByModelName(String modelName) {
        if ("qwen3:8b".equalsIgnoreCase(modelName)) {
            return qwenChatClient;
        } else if ("deepseek-r1:8b".equalsIgnoreCase(modelName)) {
            return deepseekChatClient;
        }
        return null;
    }

    private void sendResponse(WebSocketSession session, String content) {
        try {
            session.sendMessage(new TextMessage(content));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}