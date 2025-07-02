package com.player.ai.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.ai.assistant.Assistant;
import com.player.ai.entity.ChatEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.imp.ChatService;
import com.player.common.utils.JwtToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Assistant assistant;
    private final ChatMapper chatMapper;

    public ChatWebSocketHandler(Assistant assistant, ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
        this.assistant = assistant;
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
            String modelName = (String) payload.get("modelName");

            // 构造 ChatEntity
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModelName(modelName);
        assistant.chat(chatId,prompt)
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