package com.player.agent.controller;

import com.player.common.entity.ResultEntity;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.service.IAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequestMapping(value="/service/agent")
@RestController
public class AgentController {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private IAgentService agentService;

    @PostMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AgentParamsEntity agentParamsEntity

            ){
        return agentService.chat(userId, agentParamsEntity);
    }

    @GetMapping("/getChatHistory")
    public ResultEntity getChatHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ){
        return agentService.getChatHistory(userId, pageNum, pageSize);
    }

    @GetMapping("/getModelList")
    public ResultEntity getModelList( ){
        return agentService.getModelList();
    }

    @PostMapping("/uploadDoc")
    public ResultEntity uploadDoc(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId
    ) throws IOException {
        return agentService.uploadDoc(file,userId);
    }

    @GetMapping("/getDocList")
    public ResultEntity getDocList(
            @RequestHeader("X-User-Id") String userId
    ) {
        return agentService.getDocList(userId);
    }

    @DeleteMapping("/deleteDoc/{docId}")
    public ResultEntity deleteDoc(
            @PathVariable("docId") String docId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return agentService.deleteDoc(docId,userId);
    }
}
