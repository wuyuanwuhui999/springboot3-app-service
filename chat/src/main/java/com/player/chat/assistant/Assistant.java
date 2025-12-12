package com.player.chat.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import reactor.core.publisher.Flux;

public interface Assistant {
    @SystemMessage("""
        {{systemPrompt}}
        {{language}}
        """)
    Flux<String> chat(
            @MemoryId String memoryId,
            @UserMessage String userMessage,
            @V("language") String language,
            @V("systemPrompt") String systemPrompt
    );
}
