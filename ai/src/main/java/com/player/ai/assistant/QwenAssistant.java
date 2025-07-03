package com.player.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="qwenStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface QwenAssistant {
    @SystemMessage("你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
