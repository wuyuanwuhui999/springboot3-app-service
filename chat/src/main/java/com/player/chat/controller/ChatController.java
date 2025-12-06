package com.player.chat.controller;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.entity.DirectoryEntity;
import com.player.chat.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequestMapping(value="/service/chat")
@RestController
public class ChatController {

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

    @PostMapping("/uploadDoc/{tenantId}/{directoryId}")
    public ResultEntity uploadDoc(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("directoryId") String directoryId
    ) throws IOException {
        return chatService.uploadDoc(file,userId,tenantId,directoryId);
    }

    @GetMapping("/getDocListByDirId")
    public ResultEntity getDocListByDirId(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("tenantId") String tenantId,
            @RequestParam("directoryId") String directoryId
    ) {
        return chatService.getDocListByDirId(userId,tenantId,directoryId);
    }

    @GetMapping("/getDocList")
    public ResultEntity getDocList(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("tenantId") String tenantId
    ) {
        return chatService.getDocList(userId,tenantId);
    }

    @DeleteMapping("/deleteDoc/{docId}")
    public ResultEntity deleteDoc(
            @PathVariable("docId") String docId,
            @RequestParam(name = "directoryId",value = "public",required = false) String directoryId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return chatService.deleteDoc(docId,userId,directoryId);
    }

    @GetMapping("/getDirectoryList")
    public ResultEntity getDirectoryList(
            @RequestParam("tenantId") String tenantId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return chatService.getDirectoryList(userId,tenantId);
    }

    @PostMapping("/createDir")
    public ResultEntity createDir(
            @RequestBody DirectoryEntity directoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        directoryEntity.setUserId(userId);
        return chatService.createDir(directoryEntity);
    }

    @PutMapping("/renameDir")
    public ResultEntity renameDir(
            @RequestBody DirectoryEntity directoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        directoryEntity.setUserId(userId);
        return chatService.renameDir(directoryEntity);
    }

    @PutMapping("/deleteDir/{directoryId}")
    public ResultEntity renameDir(
            @RequestParam("id") long directoryId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return chatService.deleteDir(userId,directoryId);
    }
}
