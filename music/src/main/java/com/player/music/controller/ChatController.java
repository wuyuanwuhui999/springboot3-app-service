package com.player.music.controller;

import com.player.music.entity.ChatParamsEntity;
import com.player.music.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
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
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ChatParamsEntity chatParamsEntity

            ){
        return chatService.chat(userId, chatParamsEntity);
    }

    @GetMapping("/getChatHistory")
    public ResultEntity getChatHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ){
        return chatService.getChatHistory(userId, pageNum, pageSize);
    }

    @GetMapping("/getModelList")
    public ResultEntity getModelList( ){
        return chatService.getModelList();
    }

    @PostMapping("/uploadDoc")
    public ResultEntity uploadDoc(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId
    ) throws IOException {
        return chatService.uploadDoc(file,userId);
    }

    @GetMapping("/getDocList")
    public ResultEntity getDocList(
            @RequestHeader("X-User-Id") String userId
    ) {
        return chatService.getDocList(userId);
    }

    @DeleteMapping("/deleteDoc/{docId}")
    public ResultEntity deleteDoc(
            @PathVariable("docId") String docId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return chatService.deleteDoc(docId,userId);
    }
}
