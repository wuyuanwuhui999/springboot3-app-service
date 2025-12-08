package com.player.gateway.utils;

import com.player.gateway.entity.LogEntity;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LogRequestUtil {

    /**
     * 获取请求体内容
     */
    public static ServerHttpRequest wrapRequest(ServerHttpRequest request, LogEntity logEntity) {
        if (request.getHeaders().getContentLength() > 0) {
            return new ServerHttpRequestDecorator(request) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return super.getBody()
                            .doOnNext(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                String body = new String(bytes, StandardCharsets.UTF_8);
                                logEntity.setRequestBody(body.length() > 5000 ? body.substring(0, 5000) : body);
                            })
                            .map(dataBuffer -> {
                                // 重新包装DataBuffer
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return new DefaultDataBufferFactory().wrap(bytes);
                            });
                }
            };
        }
        return request;
    }

    /**
     * 获取响应体内容
     */
    public static ServerHttpResponse wrapResponse(ServerHttpResponse response, LogEntity logEntity) {
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
     * 生成请求ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
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
}