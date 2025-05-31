package com.player.music.controller;

import com.player.music.assistant.Assistant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service")
public class AssistantController {

    Assistant assistant;

    public AssistantController(Assistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/chat")
    public String assistant(@RequestParam(value = "message", defaultValue = "现在几点了") String message) {
        return assistant.chat(message);
    }
}
