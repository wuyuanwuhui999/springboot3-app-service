package com.player.agent.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.agent.config.ChatClientConfig;
import com.player.agent.config.RedisChatMemory;
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
import org.springframework.data.redis.core.RedisTemplate;
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
    private ChatClientConfig chatClientConfig;

    @Autowired
    private AgentTool agentTool;

    private final AgentMapper agentMapper;

    @Autowired
    private VectorStore vectorStore;

    public AgentWebSocketHandler(AgentMapper agentMapper) {
        this.agentMapper = agentMapper;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            // 从 WebSocket 握手时的请求头获取 userId（由 Gateway 设置）
            String userId = getUserIdFromSession(session);

            // 如果从session中获取不到userId，尝试从payload中获取
            if (userId == null || userId.isEmpty()) {
                userId = (String) payload.get("userId");
            }

            // 验证用户身份
            if (userId == null || userId.isEmpty()) {
                log.warn("User ID not found in WebSocket session or payload");
                session.sendMessage(new TextMessage("{\"error\": \"用户身份验证失败，请重新登录\"}"));
                return;
            }

            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            String modelId = (String) payload.get("modelId");
            String type = (String) payload.get("type");
            Boolean showThink = (Boolean) payload.get("showThink");
            String language = (String) payload.get("language");

            // 参数验证
            if (prompt == null || prompt.trim().isEmpty()) {
                session.sendMessage(new TextMessage("{\"error\": \"提示词不能为空\"}"));
                return;
            }

            if (modelId == null || modelId.trim().isEmpty()) {
                session.sendMessage(new TextMessage("{\"error\": \"模型ID不能为空\"}"));
                return;
            }

            // 设置默认值
            if (showThink == null) {
                showThink = false;
            }

            if (language == null || language.trim().isEmpty()) {
                language = "zh"; // 默认中文
            }

            if (type == null || type.trim().isEmpty()) {
                type = "music"; // 默认音乐类型
            }

            // 创建Agent参数实体
            AgentParamsEntity agentParamsEntity = new AgentParamsEntity();
            agentParamsEntity.setChatId(chatId);
            agentParamsEntity.setModelId(modelId);
            agentParamsEntity.setPrompt(prompt.trim());
            agentParamsEntity.setShowThink(showThink);
            agentParamsEntity.setLanguage(language);
            agentParamsEntity.setType(type);
            agentParamsEntity.setUserId(userId); // 设置用户ID

            // 创建聊天记录实体
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModelId(modelId);

            // 获取ChatClient
            ChatClient chatClient = chatClientConfig.getChatClient(modelId, new RedisChatMemory(redisTemplate));

            if (chatClient == null) {
                session.sendMessage(new TextMessage("{\"error\": \"不支持的模型ID: " + modelId + "\"}"));
                return;
            }

            log.info("开始处理用户查询 - UserId: {}, ChatId: {}, ModelId: {}, Prompt: {}",
                    userId, chatId, modelId, prompt);

            // 处理聊天请求
            Flux<String> chatStream = AgentUtils.processChat(
                    agentParamsEntity,
                    chatClient,
                    SystemtConstants.MUSIC_SYSTEMT_PROMPT,
                    agentTool
            );

            // 订阅流式响应
            String finalUserId = userId;
            String finalUserId1 = userId;
            String finalUserId2 = userId;
            chatStream.subscribe(
                    responsePart -> {
                        // 累积响应内容
                        chatEntity.setContent(chatEntity.getContent() + responsePart);

                        // 发送响应给客户端
                        sendResponse(session, responsePart);

                        // 记录调试信息
                        if (log.isDebugEnabled()) {
                            log.debug("Sent response part to user {}: {}", finalUserId,
                                    responsePart.length() > 100 ?
                                            responsePart.substring(0, 100) + "..." :
                                            responsePart);
                        }
                    },
                    throwable -> {
                        log.error("Error during streaming for user: {}", finalUserId1, throwable);
                        try {
                            String errorMessage;
                            if (throwable.getMessage().contains("timeout") ||
                                    throwable.getMessage().contains("Timeout")) {
                                errorMessage = "{\"error\": \"请求超时，请稍后重试\"}";
                            } else if (throwable.getMessage().contains("rate limit") ||
                                    throwable.getMessage().contains("Rate limit")) {
                                errorMessage = "{\"error\": \"请求频率过高，请稍后再试\"}";
                            } else {
                                errorMessage = "{\"error\": \"AI响应异常中断: " +
                                        throwable.getMessage() + "\"}";
                            }
                            session.sendMessage(new TextMessage(errorMessage));
                        } catch (IOException e) {
                            log.error("Failed to send error message to user: {}", finalUserId1, e);
                        }
                    },
                    () -> {
                        try {
                            // 保存聊天记录
                            if (chatEntity.getContent() != null &&
                                    !chatEntity.getContent().isEmpty()) {
                                agentMapper.saveChat(chatEntity);
                                log.info("聊天记录保存成功 - UserId: {}, ChatId: {}, 内容长度: {}",
                                        finalUserId2, chatId, chatEntity.getContent().length());
                            }

                            // 发送完成标记
                            session.sendMessage(new TextMessage("[DONE]"));
                            log.info("聊天会话完成 - UserId: {}, ChatId: {}", finalUserId2, chatId);

                        } catch (Exception e) {
                            log.error("保存聊天记录失败 - UserId: {}, ChatId: {}", finalUserId2, chatId, e);
                            try {
                                session.sendMessage(new TextMessage("{\"warning\": \"聊天记录保存失败，但响应已完成\"}"));
                            } catch (IOException ex) {
                                log.error("发送警告消息失败", ex);
                            }
                        }
                    }
            );

        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            try {
                String errorMessage;
                if (e.getMessage().contains("JSON")) {
                    errorMessage = "{\"error\": \"消息格式错误，请检查JSON格式\"}";
                } else {
                    errorMessage = "{\"error\": \"处理请求时发生错误: " + e.getMessage() + "\"}";
                }
                session.sendMessage(new TextMessage(errorMessage));
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    /**
     * 发送响应给客户端
     */
    private void sendResponse(WebSocketSession session, String content) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(content));
            } else {
                log.warn("尝试向已关闭的WebSocket会话发送消息");
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
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
                    log.debug("从握手头获取到用户ID: {}", userId);
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
                            String userId = param.substring("X-User-Id=".length());
                            log.debug("从查询参数获取到用户ID: {}", userId);
                            return userId;
                        }
                    }
                }
            }

            // 方法3：从 session 属性中获取
            String userId = (String) attributes.get("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                log.debug("从会话属性获取到用户ID: {}", userId);
                return userId;
            }

            // 方法4：从自定义属性获取
            userId = (String) attributes.get("userId");
            if (userId != null && !userId.isEmpty()) {
                log.debug("从自定义属性获取到用户ID: {}", userId);
                return userId;
            }

        } catch (Exception e) {
            log.error("从会话获取用户ID失败", e);
        }

        log.warn("未能在WebSocket会话中找到用户ID");
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
            log.warn("WebSocket连接被拒绝：未找到用户ID");
            try {
                session.sendMessage(new TextMessage("{\"error\": \"未授权访问，用户身份验证失败\"}"));
                Thread.sleep(100); // 等待消息发送
                session.close();
            } catch (Exception e) {
                log.error("关闭未授权连接时出错", e);
            }
            return;
        }

        // 记录连接信息
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress() != null ?
                session.getRemoteAddress().toString() : "未知地址";

        log.info("WebSocket连接建立成功 - 用户: {}, 会话ID: {}, 远程地址: {}",
                userId, sessionId, remoteAddress);

        // 将用户ID存入会话属性
        session.getAttributes().put("authenticatedUserId", userId);

        // 发送连接成功消息
        try {
            Map<String, String> welcomeMsg = Map.of(
                    "type", "connection_established",
                    "message", "连接成功，欢迎使用音乐AI助手！",
                    "userId", userId,
                    "timestamp", String.valueOf(System.currentTimeMillis())
            );
            ObjectMapper mapper = new ObjectMapper();
            session.sendMessage(new TextMessage(mapper.writeValueAsString(welcomeMsg)));
        } catch (Exception e) {
            log.warn("发送欢迎消息失败", e);
        }

        super.afterConnectionEstablished(session);
    }

    /**
     * 连接关闭时的处理
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        String sessionId = session.getId();

        log.info("WebSocket连接关闭 - 用户: {}, 会话ID: {}, 关闭状态: {}, 原因: {}",
                userId, sessionId, status.getCode(), status.getReason());

        super.afterConnectionClosed(session, status);
    }

    /**
     * 处理传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserIdFromSession(session);
        log.error("WebSocket传输错误 - 用户: {}, 会话ID: {}", userId, session.getId(), exception);

        super.handleTransportError(session, exception);
    }
}