// QwenAssistant.java
package com.player.chat.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="qwenOnlineChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface QwenOnlineAssistant extends Assistant{
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