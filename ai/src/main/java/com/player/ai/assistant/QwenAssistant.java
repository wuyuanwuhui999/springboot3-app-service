// QwenAssistant.java
package com.player.ai.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel="qwenStreamingChatModel",
        chatMemoryProvider="chatMemoryProvider"
)
public interface QwenAssistant {
    @SystemMessage("""
        你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。
        {{showThink ? '请详细解释你的思考过程' : '不要输出任何思考过程，直接给出最终答案'}}。
        {{language == 'zh' ? '请用中文回答' : 'Please respond in English'}}
        """)
    Flux<String> chat(
            @MemoryId String memoryId,
            @UserMessage String userMessage,
            @V("showThink") boolean showThink,
            @V("language") String language
    );
}