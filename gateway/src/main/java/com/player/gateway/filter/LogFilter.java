package com.player.gateway.filter;

import com.player.gateway.entity.LogEntity;
import com.player.gateway.service.imp.LogService;
import com.player.gateway.utils.CachedBodyRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class LogFilter implements GlobalFilter, Ordered {

    @Autowired
    private LogService logService;

    // 注入异步执行器（Spring会自动提供）
    @Autowired
    private Executor taskExecutor;

    private static final List<String> SENSITIVE_PATHS = List.of(
            "/service/user/login",
            "/service/user/loginByEmail",
            "/service/user/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        String requestId = CachedBodyRequestUtil.generateRequestId();

        // 创建日志实体
        LogEntity logEntity = new LogEntity();
        logEntity.setRequestId(requestId);
        logEntity.setPath(exchange.getRequest().getURI().getPath());
        logEntity.setMethod(exchange.getRequest().getMethod().name());
        logEntity.setQueryParams(exchange.getRequest().getURI().getQuery());
        logEntity.setClientIp(CachedBodyRequestUtil.getClientIp(exchange.getRequest()));

        // 获取用户ID（从JwtAuthFilter设置的header）
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        logEntity.setUserId(userId);

        // 记录请求头（排除敏感信息）
        logEntity.setRequestHeaders(extractHeaders(exchange.getRequest().getHeaders()));

        // 检查是否为敏感路径
        boolean isSensitivePath = isSensitivePath(logEntity.getPath());

        // 如果不是敏感路径，缓存请求体
        ServerWebExchange modifiedExchange = exchange;
        if (!isSensitivePath) {
            modifiedExchange = exchange.mutate()
                    .request(CachedBodyRequestUtil.cacheBodyAndRecordLog(exchange.getRequest(), logEntity))
                    .build();
        }

        // 包装响应以获取响应体
        ServerWebExchange finalExchange = modifiedExchange;
        ServerWebExchange responseExchange = modifiedExchange.mutate()
                .response(CachedBodyRequestUtil.cacheResponseAndRecordLog(
                        modifiedExchange.getResponse(), logEntity))
                .build();

        // 异步保存请求日志（不阻塞主请求）
        CompletableFuture.runAsync(() -> {
            try {
                logService.saveRequestLog(logEntity);
            } catch (Exception e) {
                System.err.println("Failed to save request log: " + e.getMessage());
                // 这里可以添加更详细的错误处理，比如写入错误日志文件
            }
        }, taskExecutor);

        return chain.filter(responseExchange).doFinally(signalType -> {
            Instant endTime = Instant.now();
            long executeTime = Duration.between(startTime, endTime).toMillis();

            String responseStatus = String.valueOf(finalExchange.getResponse().getStatusCode() != null ?
                    finalExchange.getResponse().getStatusCode().value() : 500);

            String responseHeaders = extractHeaders(finalExchange.getResponse().getHeaders());

            // 如果是敏感路径，不记录响应体
            String responseBody = isSensitivePath ? "[SENSITIVE_PATH]" : logEntity.getResponseBody();

            // 异步更新响应信息（不阻塞响应流）
            CompletableFuture.runAsync(() -> {
                try {
                    logService.updateResponseInfo(requestId, responseStatus,
                            responseBody, responseHeaders, executeTime, null);
                } catch (Exception e) {
                    System.err.println("Failed to update response log: " + e.getMessage());
                    // 这里可以添加更详细的错误处理
                }
            }, taskExecutor);
        });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 提取头部信息（排除敏感信息）
     */
    private String extractHeaders(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        Map<String, String> filteredHeaders = new HashMap<>();
        headers.forEach((key, values) -> {
            if (!key.toLowerCase().contains("authorization") &&
                    !key.toLowerCase().contains("password") &&
                    !key.toLowerCase().contains("secret") &&
                    !key.toLowerCase().contains("cookie")) {
                filteredHeaders.put(key, String.join(", ", values));
            }
        });

        return filteredHeaders.toString();
    }

    /**
     * 检查是否为敏感路径
     */
    private boolean isSensitivePath(String path) {
        return SENSITIVE_PATHS.stream().anyMatch(path::startsWith);
    }
}