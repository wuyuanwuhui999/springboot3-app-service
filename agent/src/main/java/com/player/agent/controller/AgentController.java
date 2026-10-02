package com.player.agent.controller;

import com.player.common.entity.ResultEntity;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.service.IAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequestMapping(value="/service/agent")
@RestController
public class AgentController {

    @Autowired
    private IAgentService agentService;

    /**
     * Agent 对话（HTTP 流式）
     * 入参与 WebSocket 接口 /service/agent/ws/chat 的消息一致：prompt / chatId / modelId / showThink / type / language；
     * 用户身份由网关解析 token 后通过 X-User-Id 透传（WebSocket 是握手头/查询参数）。
     */
    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
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
}
