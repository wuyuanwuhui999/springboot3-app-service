package com.player.music.controller;

import com.player.music.service.IChatService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/service")
public class ChatController {

    private final IChatService chatService;

    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam String chatId) {
        return chatService.chatStream(chatId,message);
    }
}
