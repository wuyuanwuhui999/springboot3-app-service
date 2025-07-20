package com.player.ai.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.ai.assistant.AssistantSelector;
import com.player.ai.assistant.DeepSeekAssistant;
import com.player.ai.assistant.QwenAssistant;
import com.player.ai.entity.ChatEntity;
import com.player.ai.entity.ChatParamsEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.IChatService;
import com.player.ai.utils.PromptUtil;
import com.player.common.utils.JwtToken;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IChatService chatService;

    @Value("${token.secret}")
    private String secret;

    // ChatWebSocketHandler.java
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
            Boolean showThink = (Boolean) payload.get("showThink");

            ChatParamsEntity chatParamsEntity = new ChatParamsEntity();
            chatParamsEntity.setChatId(chatId);
            chatParamsEntity.setModelName(modelName);
            chatParamsEntity.setPrompt(prompt);
            chatParamsEntity.setShowThink(showThink);
            chatParamsEntity.setType((String) payload.get("type"));
            chatParamsEntity.setLanguage((String) payload.get("language"));
            String directoryId = (String) payload.get("directoryId");
            if(StringUtil.isEmpty(directoryId)){
                directoryId = "public";
            }
            chatParamsEntity.setDirectoryId(directoryId);

            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModelName(modelName);

            // Use the new service method
            chatService.chatWithWebSocketHandling(
                    userId,
                    chatParamsEntity,
                    responsePart -> {
                        chatEntity.setContent(chatEntity.getContent() + responsePart);
                        sendResponse(session, responsePart);
                    }
            ).subscribe(
                    responsePart -> {}, // Already handled in the consumer above
                    error -> {
                        log.error("Error during streaming", error);
                        try {
                            session.sendMessage(new TextMessage("{\"error\": \"AI响应异常中断\"}"));
                        } catch (IOException e) {
                            log.error("Failed to send error message", e);
                        }
                    },
                    () -> {
                        try {
                            session.sendMessage(new TextMessage("[completed]"));
                        } catch (IOException e) {
                            log.error("Failed to send completion message", e);
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