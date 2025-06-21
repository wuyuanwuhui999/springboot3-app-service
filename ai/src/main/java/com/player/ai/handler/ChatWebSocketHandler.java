package com.player.ai.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.ai.assistant.Assistant;
import com.player.ai.entity.ChatEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.imp.ChatService;
import com.player.common.utils.JwtToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Assistant assistant;
    private final ChatMapper chatMapper;

    public ChatWebSocketHandler(Assistant assistant, ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
        this.assistant = assistant;
    }

    @Value("${token.secret}")
    private String secret;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);

            String token = (String) payload.get("token");
            String userId = JwtToken.getId(token, secret);
            String prompt = (String) payload.get("prompt");
            String chatId = (String) payload.get("chatId");
            String model = (String) payload.get("model");

            // 检查是否包含附件信息（假设前端以 base64 或其他方式传输）
            List<Map<String, String>> fileMaps = (List<Map<String, String>>) payload.getOrDefault("files", Collections.emptyList());
            List<MultipartFile> files = new ArrayList<>();

            // 这里只是示例，实际需要处理前端传来的文件数据（比如base64解码保存）
            for (Map<String, String> fileMap : fileMaps) {
                String fileName = fileMap.get("name");
                String contentType = fileMap.get("type");
                String base64Data = fileMap.get("data");

                // todo: 解码并构造 MultipartFile（可借助 Base64DecodingResource）
            }

            // 构造 ChatEntity
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModel(model);
        assistant.chat(chatId,prompt)
                .subscribe(
                    responsePart -> {
                        chatEntity.setContent(chatEntity.getContent() + responsePart);
                        sendResponse(session, responsePart);
                    },
                    throwable -> {
                        log.error("Error during streaming", throwable);
                        try {
                            session.sendMessage(new TextMessage("{\"error\": \"AI响应异常中断\"}"));
                        } catch (IOException e) {
                            log.error("Failed to send error message", e);
                        }
                    },
                    () -> {
                        // 所有数据接收完毕后保存数据库
                        chatMapper.saveChat(chatEntity);
                        try {
                            session.sendMessage(new TextMessage("[completed]"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            try {
                session.sendMessage(new TextMessage("{\"error\": \"" + e.getMessage() + "\"}"));
            } catch (IOException ex) {
                log.error("Failed to send error message", ex);
            }
        }
    }

    private void sendResponse(WebSocketSession session, String content) {
        try {
            session.sendMessage(new TextMessage(content));
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }
}