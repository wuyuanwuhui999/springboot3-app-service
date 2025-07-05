package com.player.ai.controller;

import com.player.ai.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.UserEntity;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
@RequestMapping(value="/service/ai")
@RestController
public class ChatController {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private IChatService chatService;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(
            @RequestHeader("Authorization") String token,
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            @RequestParam("modelName") String modelName,
            @RequestParam("showThink") boolean showThink
    ){
        return chatService.chat(JwtToken.getId(token, secret), prompt, chatId, modelName,showThink);
    }

    @GetMapping("/getChatHistory")
    public ResultEntity getChatHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ){
        ResultEntity chatHistory = chatService.getChatHistory(JwtToken.getId(token, secret), pageNum, pageSize);
        return chatHistory;
    }
}
