// DeepSeekAssistant.java
package com.player.ai.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="deepSeekStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface DeepSeekAssistant {
    @SystemMessage("""
        你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。
        {{language}}
        """)
    Flux<String> chat(
            @MemoryId String memoryId,
            @UserMessage String userMessage,
            @V("language") String language
    );
}