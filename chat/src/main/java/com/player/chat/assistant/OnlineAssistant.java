package com.player.chat.assistant;

import reactor.core.publisher.Flux;

public interface OnlineAssistant {
    Flux<String> chat(String memoryId, String prompt, String language, String apiKey, String baseUrl);
}