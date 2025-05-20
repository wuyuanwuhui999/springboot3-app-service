package com.player.ai.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.ai.entity.ChatEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.imp.ChatService;
import com.player.common.utils.JwtToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatClient chatClient;
    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final String uploadDir;

    public ChatWebSocketHandler(ChatClient chatClient, ChatService chatService, ChatMapper chatMapper, String uploadDir) {
        this.chatClient = chatClient;
        this.chatService = chatService;
        this.chatMapper = chatMapper;
        this.uploadDir = uploadDir;
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
            Flux<String> stringFlux;
            if (files.isEmpty()) {
                chatClient.prompt()
                        .user(prompt)
                        .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                        .stream()
                        .content()
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
                                if (chatEntity.getContent() != null && !chatEntity.getContent().isEmpty()) {
                                    chatMapper.saveChat(chatEntity);
                                }
                            }
                    );

            } else {
                // 类似原来的多模态处理逻辑
                String uploadedFiles = chatService.upload(files);
                chatEntity.setFiles(uploadedFiles);

                List<Media> medias = files.stream()
                        .map(file -> new Media(MimeType.valueOf(file.getContentType()), file.getResource()))
                        .toList();

                chatClient.prompt()
                        .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                        .advisors(a -> a.param("conversationId", chatId))
                        .stream()
                        .content()
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
                                    if (chatEntity.getContent() != null && !chatEntity.getContent().isEmpty()) {
                                        chatMapper.saveChat(chatEntity);
                                    }
                                }
                        );
            }

            // 保存到数据库
            chatMapper.saveChat(chatEntity);

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