package com.player.chat.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

public interface Assistant {
    Flux<String> chat(
            String memoryId,
            String userMessage,
            String language,
            String systemPrompt
    );
}
