// QwenAssistant.java
package com.player.chat.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="qwenStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface QwenOllamaAssistant {
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