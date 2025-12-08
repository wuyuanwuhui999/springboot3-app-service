package com.player.chat.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 将请求头中的 X-User-Id 保存到 WebSocket 会话属性中
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            attributes.put("X-User-Id", userId);
        }

        // 保存整个请求头到会话属性中
        attributes.put("handshakeHeaders", request.getHeaders());

        // 保存查询参数中的 userId（备用）
        String query = request.getURI().getQuery();
        if (query != null && query.contains("X-User-Id=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("X-User-Id=")) {
                    String queryUserId = param.substring("X-User-Id=".length());
                    attributes.put("queryUserId", queryUserId);
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不需要做任何处理
    }
}