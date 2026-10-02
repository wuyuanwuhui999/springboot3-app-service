package com.player.agent.service;

import com.player.agent.entity.AgentParamsEntity;
import com.player.common.entity.ResultEntity;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

public interface IAgentService {
    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    /**
     * Agent 对话（HTTP 流式）
     *
     * @param userId            当前登录用户（由网关 X-User-Id 透传）
     * @param agentParamsEntity 聊天参数（与 WebSocket 接口一致）
     * @return 流式返回的回答文本
     */
    Flux<String> chat(String userId, AgentParamsEntity agentParamsEntity);

    /**
     * Agent 对话的公共处理逻辑（WebSocket 与 HTTP 流式接口共用）
     *
     * @param responseHandler 每个响应分片的回调（WebSocket 用于推送给客户端；HTTP 传空实现即可）
     * @return 流式返回的回答文本
     */
    Flux<String> chatWithWebSocketHandling(String userId, AgentParamsEntity agentParamsEntity,
                                           Consumer<String> responseHandler);
}
