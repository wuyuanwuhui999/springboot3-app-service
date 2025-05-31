package com.player.music.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService()
public interface Assistant {
    @SystemMessage("你叫小吴同学，是一个AI助手")
    String chat(String userMessage);
}
