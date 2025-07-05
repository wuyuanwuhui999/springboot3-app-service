package com.player.ai.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="deepSeekStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface DeepSeekAssistant {
    @SystemMessage("""
        你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。
        当showThink为false时，不要输出任何思考过程，直接给出最终答案。
        """)
    Flux<String> chat(
            @MemoryId String memoryId,
            @UserMessage String userMessage,
            @V("showThink") boolean showThink
    );
}
