// DeepSeekAssistant.java
package com.player.gateway.chat.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="deepSeekStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface DeepSeekOllamaAssistant {
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