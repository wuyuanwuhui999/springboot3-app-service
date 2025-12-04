package com.player.gateway.circle.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Bean
    MyWebSocketHandler webSocketHandler(){
        return new MyWebSocketHandler();
    }

    // 保存所有连接的会话
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 当客户端连接成功时触发
        sessions.add(session);
        System.out.println("新客户端连接：" + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 当收到客户端消息时触发
        String payload = message.getPayload();
        System.out.println("收到消息：" + payload);

        // 将消息广播给所有连接的客户端
        broadcastMessage(payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        // 当客户端断开连接时触发
        sessions.remove(session);
        System.out.println("客户端断开：" + session.getId());
    }

    // 广播消息到所有客户端
    public void broadcastMessage(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}