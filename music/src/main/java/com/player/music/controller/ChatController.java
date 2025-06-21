package com.player.music.controller;

import com.player.music.entity.FileEntity;
import com.player.music.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@RequestMapping(value="/service/music")
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
            @RequestParam("chatId") int modelId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ){
        return chatService.chat(JwtToken.getId(token, secret), prompt, chatId,modelId, files);
    }

    @GetMapping("/getChatHistory")
    public ResultEntity getChatHistory(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ){
        return chatService.getChatHistory(JwtToken.getId(token, secret), pageNum, pageSize);
    }

    @GetMapping("/getModelList")
    public ResultEntity getModelList( ){
        return chatService.getModelList();
    }

    @PostMapping("/generateVector")
    public ResultEntity generateVector(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return chatService.generateVector(file);
    }
}
