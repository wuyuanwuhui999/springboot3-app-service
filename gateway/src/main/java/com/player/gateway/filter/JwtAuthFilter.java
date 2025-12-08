package com.player.gateway.filter;

import com.player.common.entity.UserEntity;
import com.player.common.utils.JwtToken;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

@Component
@Order(-1)
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> WHITE_LIST = List.of(
            "/service/user/register",
            "/service/user/login",
            "/service/user/loginByEmail",
            "/service/user/vertifyUser",
            "/service/user/sendEmailVertifyCode"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String protocol = exchange.getRequest().getHeaders().getFirst("Upgrade");

        // 生成请求ID并添加到header
        String requestId = UUID.randomUUID().toString().replace("-", "");
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Request-ID", requestId)
                        .build())
                .build();

        // WebSocket 连接检查
        boolean isWebSocket = "websocket".equalsIgnoreCase(protocol);

        if (isWebSocket) {
            return handleWebSocketAuth(modifiedExchange, chain, path);
        } else {
            return handleHttpAuth(modifiedExchange, chain, path);
        }
    }

    private Mono<Void> handleWebSocketAuth(ServerWebExchange exchange, GatewayFilterChain chain, String path) {
        // WebSocket 白名单检查（如果需要的话）
        if (isWebSocketWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 从查询参数或Header中获取Token
        String token = getWebSocketToken(exchange);

        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "Missing token for WebSocket connection");
        }

        try {
            UserEntity user = JwtToken.parseToken(token, UserEntity.class, jwtSecret);
            if (user == null || user.getId() == null) {
                return unauthorized(exchange, "Invalid token for WebSocket connection");
            }

            // 将 userId 添加到查询参数中（确保能传递到后端）
            String userId = user.getId();
            URI originalUri = exchange.getRequest().getURI();

            // 构建新的 URI，包含 userId 参数
            String query = originalUri.getQuery();
            String newQuery = query == null ?
                    "X-User-Id=" + userId :
                    query + "&X-User-Id=" + userId;

            URI newUri = UriComponentsBuilder.fromUri(originalUri)
                    .replaceQuery(newQuery)
                    .build()
                    .toUri();

            ServerWebExchange userExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .uri(newUri)
                            .header("X-User-Id", userId)
                            .build())
                    .build();

            return chain.filter(userExchange);

        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token for WebSocket connection");
        }
    }

    private Mono<Void> handleHttpAuth(ServerWebExchange exchange, GatewayFilterChain chain, String path) {
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            UserEntity user = JwtToken.parseToken(token, UserEntity.class, jwtSecret);
            if (user == null || user.getId() == null) {
                return unauthorized(exchange, "Invalid token: user not found");
            }

            ServerWebExchange userExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id", user.getId())
                            .build())
                    .build();

            return chain.filter(userExchange);

        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private String getWebSocketToken(ServerWebExchange exchange) {
        // 1. 尝试从查询参数获取（常见方式：ws://host/path?token=xxx）
        String query = exchange.getRequest().getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        // 2. 尝试从Sec-WebSocket-Protocol头获取（备用方式）
        String protocolHeader = exchange.getRequest().getHeaders().getFirst("Sec-WebSocket-Protocol");
        if (protocolHeader != null && protocolHeader.contains("token=")) {
            String[] protocols = protocolHeader.split(",");
            for (String protocol : protocols) {
                protocol = protocol.trim();
                if (protocol.startsWith("token=")) {
                    return protocol.substring(6);
                }
            }
        }

        // 3. 尝试从Authorization头获取（兼容HTTP方式）
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private boolean isWebSocketWhiteListPath(String path) {
        // 如果有WebSocket特定的白名单，可以在这里添加
        // 例如：return path.startsWith("/service/chat/ws/public");
        return false; // 默认所有WebSocket都需要认证
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\":\"Unauthorized\", \"message\":\"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}