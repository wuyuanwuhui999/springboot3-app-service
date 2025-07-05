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
            @RequestParam("modelName") String modelName
    ){
        return chatService.chat(JwtToken.getId(token, secret), prompt, chatId,modelName);
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

    @PostMapping("/uploadDoc")
    public ResultEntity uploadDoc(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token
    ) throws IOException {
        return chatService.uploadDoc(file,JwtToken.getId(token, secret));
    }

    @GetMapping("/searchDoc")
    public Flux<String> searchDoc(
            @RequestParam("query") String query,
            @RequestParam("chatId") String chatId,
            @RequestParam("modelName") String modelName,
            @RequestHeader("Authorization") String token
    ) {
        return chatService.searchDoc(query,chatId,JwtToken.getId(token, secret),modelName);
    }

    @GetMapping("/getDocList")
    public ResultEntity getDocList(
            @RequestHeader("Authorization") String token
    ) {
        return chatService.getDocList(JwtToken.getId(token, secret));
    }
}
