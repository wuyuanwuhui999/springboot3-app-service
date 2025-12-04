package com.player.gateway.music.controller;

import com.player.gateway.music.entity.ChatParamsEntity;
import com.player.gateway.music.service.IChatService;
import com.player.gateway.common.entity.ResultEntity;
import com.player.gateway.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequestMapping(value="/service/music")
@RestController
public class ChatController {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private IChatService chatService;

    @PostMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(
            @RequestHeader("Authorization") String token,
            @RequestBody ChatParamsEntity chatParamsEntity

            ){
        return chatService.chat(JwtToken.getId(token, secret), chatParamsEntity);
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

    @GetMapping("/getDocList")
    public ResultEntity getDocList(
            @RequestHeader("Authorization") String token
    ) {
        return chatService.getDocList(JwtToken.getId(token, secret));
    }

    @DeleteMapping("/deleteDoc/{docId}")
    public ResultEntity deleteDoc(
            @PathVariable("docId") String docId,
            @RequestHeader("Authorization") String token
    ) {
        return chatService.deleteDoc(docId,JwtToken.getId(token, secret));
    }
}
