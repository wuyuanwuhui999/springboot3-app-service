package com.player.music.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.music.entity.ChatEntity;
import com.player.music.mapper.ChatMapper;
import com.player.common.utils.JwtToken;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final StreamingChatModel chatModel;
    private final Map<String, MessageWindowChatMemory> chatMemories = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(StreamingChatModel chatModel, ChatMapper chatMapper) {
        this.chatModel = chatModel;
        this.chatMapper = chatMapper;
    }

    private final ChatMapper chatMapper;

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

            // 构造 ChatEntity
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatEntity.setUserId(userId);
            chatEntity.setPrompt(prompt);
            chatEntity.setContent("");
            chatEntity.setModel(model);
            // 获取或创建会话记忆
            MessageWindowChatMemory memory = chatMemories.computeIfAbsent(chatId, k -> MessageWindowChatMemory.withMaxMessages(10));

            // 添加用户消息到记忆中
            UserMessage userMessage = new UserMessage(prompt);
            memory.add(userMessage);

            // 构建完整的消息历史
            List<ChatMessage> chatHistory = new ArrayList<>();
            chatHistory.add(new SystemMessage("你是一个热心、可爱的智能助手，你的名字叫小团团，请以小团团的身份和语气回答问题"));
            chatHistory.addAll(memory.messages());
            CompletableFuture<Void> future = new CompletableFuture<>();
            chatModel.chat(chatHistory, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.print(partialResponse);
                    try {
                        session.sendMessage(new TextMessage(partialResponse));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    future.complete(null);
                    // 添加AI消息到记忆
                    memory.add(AiMessage.from(chatResponse.aiMessage().text()));
                    chatEntity.setContent(chatResponse.aiMessage().text());
                    // 保存到数据库
                    chatMapper.saveChat(chatEntity);
                }

                @Override
                public void onError(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });


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