package com.player.agent.controller;

import com.player.common.entity.ResultEntity;
import com.player.agent.service.IAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value="/service/agent")
@RestController
public class AgentController {

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
}
