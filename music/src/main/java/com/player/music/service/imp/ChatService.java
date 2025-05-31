package com.player.music.service.imp;
import com.player.music.service.IChatService;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatService implements IChatService {

    private StreamingChatModel streamingChatModel;

    @Autowired
    public ChatService(StreamingChatModel chatModel,
                           List<ToolSpecification> toolSpecifications) {
        this.streamingChatModel = chatModel;
    }

    @Override
    public Flux<String> chatStream(String chatId, String message) {
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        return Flux.create(emitter -> {
            streamingChatModel.chat(message, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    futureResponse.complete(completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    futureResponse.completeExceptionally(error);
                }
            });

            futureResponse.join();
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}