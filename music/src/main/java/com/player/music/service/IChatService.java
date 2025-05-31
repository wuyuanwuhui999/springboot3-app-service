package com.player.music.service;

import com.player.music.dto.ChatRequest;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

public interface IChatService {
    Flux<String> chatStream(String chatId, String message);
}