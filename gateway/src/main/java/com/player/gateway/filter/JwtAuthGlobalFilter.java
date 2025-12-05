package com.player.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Component
@Order(-1) // 确保在其他 filter 之前执行
public class JwtAuthGlobalFilter implements GlobalFilter {

    // 👇 替换为你自己的密钥（建议从配置文件读取）
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // 不需要认证的路径前缀（白名单）
    private static final List<String> WHITE_LIST = List.of(
            "/service/use/"
    );

    // 需要认证的路径前缀（可选，也可反向判断）
    private static final List<String> AUTH_REQUIRED_PATHS = List.of(
            "/service/user-gateway/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 如果是白名单路径（如登录），直接放行
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 2. 如果不是需要认证的路径，也可以选择放行（按需调整）
        // 这里我们只对 /service/user-gateway/ 强制校验
        if (!isAuthRequiredPath(path)) {
            return chain.filter(exchange); // 非敏感路径也放行（比如健康检查）
        }

        // 3. 提取 Authorization 头
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // 去掉 "Bearer "

        try {
            // 4. 验证 JWT
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 可选：将用户信息写入请求头，供下游服务使用
            String userId = claims.getSubject(); // 假设 subject 是用户 ID
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build())
                    .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token: " + e.getMessage());
        }
    }

    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private boolean isAuthRequiredPath(String path) {
        return AUTH_REQUIRED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerWebExchange responseExchange = exchange.mutate()
                .response(exchange.getResponse().mutate()
                        .statusCode(HttpStatus.UNAUTHORIZED)
                        .build())
                .build();

        // 可选：写入错误信息到响应体（简单示例）
        byte[] bytes = ("{\"error\":\"Unauthorized\", \"message\":\"" + message + "\"}").getBytes();
        responseExchange.getResponse().getHeaders().add("Content-Type", "application/json");
        return responseExchange.getResponse().writeWith(Mono.just(responseExchange.getResponse().bufferFactory().wrap(bytes)));
    }
}