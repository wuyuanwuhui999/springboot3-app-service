package com.player.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.chat.entity.ChatEntity;
import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private IChatService chatService;

    // ChatWebSocketHandler.java
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            // 从 WebSocket 握手时的请求头获取 userId（由 Gateway 设置）
            String userId = getUserIdFromSession(session);

            if (userId == null || userId.isEmpty()) {
                session.sendMessage(new TextMessage("{\"error\": \"未授权访问\"}"));
                session.close();
                return;
            }

            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            String modelId = (String) payload.get("modelId");
            String tenantId = (String) payload.get("tenantId");
            Boolean showThink = (Boolean) payload.get("showThink");
            ChatParamsEntity chatParamsEntity = new ChatParamsEntity();

            chatParamsEntity.setUserId(userId);
            chatParamsEntity.setChatId(chatId);
            chatParamsEntity.setModelId(modelId);
            chatParamsEntity.setPrompt(prompt);
            chatParamsEntity.setShowThink(showThink);
            chatParamsEntity.setTenantId(tenantId);
            chatParamsEntity.setType((String) payload.get("type"));
            chatParamsEntity.setLanguage((String) payload.get("language"));
            chatParamsEntity.setSystemPrompt((String) payload.get("systemPrompt"));
            if("document".equals(chatParamsEntity.getType())){
                chatParamsEntity.setDocIds((ArrayList<String>)payload.get("docIds"));
            }
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setTenantId(tenantId);
            chatEntity.setContent("");
            chatEntity.setModelId(modelId);

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
                            try {
                                session.sendMessage(new TextMessage("[completed]"));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } finally {

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

    /**
     * 从 WebSocket 会话中获取 userId
     * Gateway 会在握手请求头中设置 X-User-Id
     */
    private String getUserIdFromSession(WebSocketSession session) {
        try {
            // 方法1：从握手时的 HTTP 请求头获取
            Map<String, Object> attributes = session.getAttributes();
            HttpHeaders handshakeHeaders = (HttpHeaders) attributes.get("handshakeHeaders");
            if (handshakeHeaders != null) {
                String userId = handshakeHeaders.getFirst("X-User-Id");
                if (userId != null && !userId.isEmpty()) {
                    return userId;
                }
            }

            // 方法2：从查询参数获取（备用方案）
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("X-User-Id=")) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("X-User-Id=")) {
                            return param.substring("X-User-Id=".length());
                        }
                    }
                }
            }

            // 方法3：从 session 属性中获取
            String userId = (String) attributes.get("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }

        } catch (Exception e) {
            log.error("Failed to get userId from session", e);
        }
        return null;
    }

    /**
     * 在连接建立时验证用户身份
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 验证用户身份
        String userId = getUserIdFromSession(session);
        if (userId == null || userId.isEmpty()) {
            log.warn("WebSocket connection rejected: No user id found");
            session.sendMessage(new TextMessage("{\"error\": \"未授权访问\"}"));
            session.close();
            return;
        }

        log.info("WebSocket connection established for user: {}", userId);
        super.afterConnectionEstablished(session);
    }

    private void sendResponse(WebSocketSession session, String content) {
        try {
            session.sendMessage(new TextMessage(content));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}