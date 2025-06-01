package com.player.music.service.imp;

import com.player.music.service.IChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatService implements IChatService {

    private final StreamingChatModel chatModel;
    private final Map<String, MessageWindowChatMemory> chatMemories = new ConcurrentHashMap<>();

    public ChatService(StreamingChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public Flux<String> chatStream(String chatId, String message) {
        return Flux.create(emitter -> {
            try {
                // 获取或创建会话记忆
                MessageWindowChatMemory memory = chatMemories.computeIfAbsent(chatId, k -> MessageWindowChatMemory.withMaxMessages(10));

                // 添加用户消息到记忆中
                UserMessage userMessage = new UserMessage(message);
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
                        emitter.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse chatResponse) {
                        future.complete(null);
                        // 添加AI消息到记忆
                        memory.add(AiMessage.from(chatResponse.aiMessage().text()));
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.error(throwable);
                        future.completeExceptionally(throwable);
                    }
                });
                future.join(); // 等待完成
            } catch (Exception e) {
                emitter.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}