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
}
