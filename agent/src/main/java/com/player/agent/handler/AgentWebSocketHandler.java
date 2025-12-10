package com.player.agent.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.agent.mapper.AgentMapper;
import com.player.agent.tool.AgentTool;
import com.player.common.entity.ChatEntity;
import com.player.agent.constants.SystemtConstants;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.uitls.AgentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Component
@Slf4j
public class AgentWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    @Qualifier("qwenOllamaChatClient")
    private ChatClient qwenOllamaChatClient;

    @Autowired
    @Qualifier("deepseekOllamaChatClient")
    private ChatClient deepseekOllamaChatClient;

    @Autowired
    private AgentTool agentTool;

    private final AgentMapper agentMapper;
    @Autowired
    private VectorStore vectorStore;

    public AgentWebSocketHandler(AgentMapper agentMapper) {
        this.agentMapper = agentMapper;
    }

    @Value("${token.secret}")
    private String secret;
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            // 从 WebSocket 握手时的请求头获取 userId（由 Gateway 设置）
            String userId = getUserIdFromSession(session);

            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            String modelName = (String) payload.get("modelName");
            String type = (String) payload.get("type");
            boolean showThink = (boolean) payload.get("showThink");
            String language = (String) payload.get("language");

            AgentParamsEntity agentParamsEntity = new AgentParamsEntity();
            agentParamsEntity.setChatId(chatId);
            agentParamsEntity.setModelName(modelName);
            agentParamsEntity.setPrompt(prompt);
            agentParamsEntity.setShowThink(showThink);
            agentParamsEntity.setLanguage(language);
            agentParamsEntity.setType(type);

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

            Flux<String> chatStream  = AgentUtils.processChat(
                    agentParamsEntity,
                    chatClient,
                    vectorStore,
                    userId,
                    SystemtConstants.MUSIC_SYSTEMT_PROMPT,
                    agentTool  // Added agentTool parameter
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
                        agentMapper.saveChat(chatEntity);
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
            return qwenOllamaChatClient;
        } else if ("deepseek-r1:8b".equalsIgnoreCase(modelName)) {
            return deepseekOllamaChatClient;
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
}