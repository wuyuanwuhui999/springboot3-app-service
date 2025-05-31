package com.player.music.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface StreamingAssistant {
    @SystemMessage("${custom.ollama.system-prompt}")
    TokenStream chat(@UserMessage String userMessage);
}
