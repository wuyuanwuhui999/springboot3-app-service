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
import reactor.core.publisher.Mono;

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

        // 生成请求ID并添加到header
        String requestId = UUID.randomUUID().toString().replace("-", "");
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Request-ID", requestId)
                        .build())
                .build();

        if (isWhiteListPath(path)) {
            return chain.filter(modifiedExchange);
        }

        String authHeader = modifiedExchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(modifiedExchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            UserEntity user = JwtToken.parseToken(token, UserEntity.class, jwtSecret);
            if (user == null || user.getId() == null) {
                return unauthorized(modifiedExchange, "Invalid token: user not found");
            }

            ServerWebExchange userExchange = modifiedExchange.mutate()
                    .request(modifiedExchange.getRequest().mutate()
                            .header("X-User-Id", user.getId())
                            .build())
                    .build();

            return chain.filter(userExchange);

        } catch (Exception e) {
            return unauthorized(modifiedExchange, "Invalid or expired token");
        }
    }

    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
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