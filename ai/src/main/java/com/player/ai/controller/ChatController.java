package com.player.ai.controller;

import com.player.ai.entity.ChatParamsEntity;
import com.player.ai.entity.DirectoryEntity;
import com.player.ai.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.UserEntity;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
@RequestMapping(value="/service/ai")
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
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "directoryId",value = "public",required = false) String directoryId
    ) throws IOException {
        return chatService.uploadDoc(file,JwtToken.getId(token, secret),directoryId);
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
            @RequestParam(name = "directoryId",value = "public",required = false) String directoryId,
            @RequestHeader("Authorization") String token
    ) {
        return chatService.deleteDoc(docId,JwtToken.getId(token, secret),directoryId);
    }

    @GetMapping("/getDirectoryList")
    public ResultEntity getDirectoryList(
            @RequestParam("tenantId") String tenantId,
            @RequestHeader("Authorization") String token
    ) {
        return chatService.getDirectoryList(JwtToken.getId(token, secret),tenantId);
    }

    @PostMapping("/createDir")
    public ResultEntity createDir(
            @RequestBody DirectoryEntity directoryEntity,
            @RequestHeader("Authorization") String token
    ) {
        directoryEntity.setUserId(JwtToken.getId(token, secret));
        return chatService.createDir(directoryEntity);
    }

    @PutMapping("/renameDir")
    public ResultEntity renameDir(
            @RequestBody DirectoryEntity directoryEntity,
            @RequestHeader("Authorization") String token
    ) {
        directoryEntity.setUserId(JwtToken.getId(token, secret));
        return chatService.renameDir(directoryEntity);
    }

    @PutMapping("/deleteDir/{directoryId}")
    public ResultEntity renameDir(
            @RequestParam("id") long directoryId,
            @RequestHeader("Authorization") String token
    ) {
        return chatService.deleteDir(JwtToken.getId(token, secret),directoryId);
    }
}
