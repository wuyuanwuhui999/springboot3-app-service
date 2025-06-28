package com.player.music.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.music.entity.ChatEntity;
import com.player.music.mapper.ChatMapper;
import com.player.music.service.imp.ChatService;
import com.player.common.utils.JwtToken;
import com.player.music.uitls.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatClient chatClient;
    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final String uploadDir;
    @Autowired
    private VectorStore vectorStore;

    public ChatWebSocketHandler(ChatClient chatClient, ChatService chatService, ChatMapper chatMapper, String uploadDir) {
        this.chatClient = chatClient;
        this.chatService = chatService;
        this.chatMapper = chatMapper;
        this.uploadDir = uploadDir;
    }

    @Value("${token.secret}")
    private String secret;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            String token = (String) payload.get("token");
            String userId = JwtToken.getId(token, secret);
            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            int modelId = (int) payload.get("modelId");
            String type = (String) payload.get("type");
            if("document".equals(type)) {
                // 1. 从向量库检索相关文档
                List<Document> relevantDocs = vectorStore.similaritySearch(prompt);
                // 3. 构建完整提示词
                // 2. 构建上下文提示
                String context =  PromptUtil.buildContext(relevantDocs);
                // 3. 构建完整提示词
                prompt = PromptUtil.buildPrompt(prompt, context);
            }
            // 构造 ChatEntity
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModelId(modelId);
            chatClient.prompt()
                    .user(prompt)
                    .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                    .stream()
                    .content()
                    .subscribe(
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
                                // 所有数据接收完毕后保存数据库
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

    private void sendResponse(WebSocketSession session, String content) {
        try {
            session.sendMessage(new TextMessage(content));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}