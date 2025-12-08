package com.player.gateway.filter;

import com.player.common.entity.UserEntity;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WebSocketAuthFilter extends AbstractGatewayFilterFactory<WebSocketAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public WebSocketAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 检查是否为 WebSocket 连接
            String upgradeHeader = request.getHeaders().getFirst("Upgrade");
            boolean isWebSocket = upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket");

            if (!isWebSocket || config.getExcludePaths().stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            // 获取 Token
            String token = extractWebSocketToken(request);

            if (token == null || token.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                UserEntity user = JwtToken.parseToken(token, UserEntity.class, jwtSecret);
                if (user == null || user.getId() == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // 添加用户信息到请求头
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", user.getId())
                        .header("X-User-Name", user.getUsername() != null ? user.getUsername() : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String extractWebSocketToken(ServerHttpRequest request) {
        // 从查询参数获取
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        // 从协议头获取
        String protocolHeader = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        if (protocolHeader != null) {
            String[] protocols = protocolHeader.split(",");
            for (String protocol : protocols) {
                protocol = protocol.trim();
                if (protocol.startsWith("token=")) {
                    return protocol.substring(6);
                }
            }
        }

        return null;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("excludePaths");
    }

    public static class Config {
        private List<String> excludePaths = List.of();

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
}