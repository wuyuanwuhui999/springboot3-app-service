package com.player.circle.config;

import com.player.circle.handler.MyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 处理器，并指定路径
        registry.addHandler(new MyWebSocketHandler(), "/service/circle/ws")
                .setAllowedOrigins("*"); // 允许跨域访问
    }
}
