package com.player.gateway.utils;

import com.player.gateway.entity.LogEntity;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CachedBodyRequestUtil {

    /**
     * 缓存请求体并记录到日志
     */
    public static ServerHttpRequest cacheBodyAndRecordLog(ServerHttpRequest request, LogEntity logEntity) {
        // 如果是GET请求或没有body，直接返回
        if (HttpMethod.GET.equals(request.getMethod()) ||
                HttpMethod.HEAD.equals(request.getMethod()) ||
                request.getHeaders().getContentLength() <= 0) {
            return request;
        }

        // 原子引用用于存储body
        AtomicReference<String> cachedBody = new AtomicReference<>("");

        return new ServerHttpRequestDecorator(request) {
            private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
            private boolean bodyRead = false;

            @Override
            public Flux<DataBuffer> getBody() {
                if (bodyRead) {
                    // 如果已经读取过，返回缓存的body
                    return Flux.just(bufferFactory.wrap(cachedBody.get().getBytes(StandardCharsets.UTF_8)));
                }

                return super.getBody()
                        .collectList()
                        .flatMapMany(dataBuffers -> {
                            StringBuilder bodyBuilder = new StringBuilder();
                            dataBuffers.forEach(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                bodyBuilder.append(new String(bytes, StandardCharsets.UTF_8));
                            });

                            String body = bodyBuilder.toString();
                            cachedBody.set(body);
                            bodyRead = true;

                            // 记录到日志实体，限制长度
                            logEntity.setRequestBody(body.length() > 5000 ? body.substring(0, 5000) : body);

                            // 返回新的DataBuffer
                            return Flux.just(bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8)));
                        });
            }
        };
    }

    /**
     * 缓存响应体并记录到日志
     */
    public static ServerHttpResponse cacheResponseAndRecordLog(ServerHttpResponse response, LogEntity logEntity) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        StringBuilder sb = new StringBuilder();
                        dataBuffers.forEach(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            sb.append(new String(bytes, StandardCharsets.UTF_8));
                        });
                        String responseBody = sb.toString();
                        logEntity.setResponseBody(responseBody.length() > 5000 ? responseBody.substring(0, 5000) : responseBody);
                        return response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                    }));
                }
                return super.writeWith(body);
            }
        };
    }

    /**
     * 获取客户端IP
     */
    public static String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 生成请求ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}