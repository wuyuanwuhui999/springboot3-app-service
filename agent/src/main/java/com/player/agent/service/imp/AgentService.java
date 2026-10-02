package com.player.agent.service.imp;

import com.player.agent.config.ChatClientConfig;
import com.player.agent.config.MongoChatMemory;
import com.player.agent.constants.SystemtConstants;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.mapper.AgentMapper;
import com.player.agent.uitls.AgentUtils;
import com.player.agent.service.IAgentService;
import com.player.common.entity.ChatEntity;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Service
public class AgentService implements IAgentService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private ChatClientConfig chatClientConfig;

    @Autowired
    private MongoChatMemory mongoChatMemory;

    @Override
    public ResultEntity getChatHistory(String userId, int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        ResultEntity success = ResultUtil.success(agentMapper.getChatHistory(userId, start, pageSize));
        success.setTotal(agentMapper.getChatHistoryTotal(userId));
        return success;
    }

    @Override
    public ResultEntity getModelList() {
        return ResultUtil.success(agentMapper.getModelList());
    }

    @Override
    public Flux<String> chat(String userId, AgentParamsEntity agentParamsEntity) {
        return chatWithWebSocketHandling(userId, agentParamsEntity, responsePart -> {
            // HTTP 流式：分片直接由 Spring MVC 写给客户端，无需额外处理
        });
    }

    @Override
    public Flux<String> chatWithWebSocketHandling(String userId, AgentParamsEntity agentParamsEntity,
                                                  Consumer<String> responseHandler) {
        agentParamsEntity.setUserId(userId);

        // 会话记忆：ChatClient 内部挂 MessageChatMemoryAdvisor（MongoDB 存储）
        ChatClient chatClient = chatClientConfig.getChatClient(agentParamsEntity.getModelId(), mongoChatMemory);
        if (chatClient == null) {
            return Flux.error(new IllegalArgumentException("不支持的模型ID: " + agentParamsEntity.getModelId()));
        }

        // 聊天记录实体（MySQL 双写：chat_history 表）
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(userId);
        chatEntity.setChatId(agentParamsEntity.getChatId());
        chatEntity.setPrompt(agentParamsEntity.getPrompt());
        chatEntity.setModelId(agentParamsEntity.getModelId());
        chatEntity.setContent("");

        StringBuilder responseCollector = new StringBuilder();

        log.info("开始处理Agent请求 - UserId: {}, ChatId: {}, ModelId: {}, Prompt: {}",
                userId, agentParamsEntity.getChatId(), agentParamsEntity.getModelId(), agentParamsEntity.getPrompt());

        return AgentUtils.processChat(agentParamsEntity, chatClient, SystemtConstants.MUSIC_SYSTEMT_PROMPT)
                .doOnNext(responsePart -> {
                    // 累积响应内容
                    responseCollector.append(responsePart);
                    chatEntity.setContent(responseCollector.toString());
                    // 分片回调（WebSocket 推送；HTTP 为空实现）
                    if (responseHandler != null) {
                        responseHandler.accept(responsePart);
                    }
                })
                .doOnComplete(() -> {
                    chatEntity.setContent(responseCollector.toString());
                    if (responseCollector.length() > 0) {
                        agentMapper.saveChat(chatEntity);
                        log.info("聊天记录保存成功(MySQL) - UserId: {}, ChatId: {}, 内容长度: {}",
                                userId, chatEntity.getChatId(), responseCollector.length());
                    }
                })
                .doOnError(e -> {
                    log.error("流式响应异常 - UserId: {}, ChatId: {}", userId, chatEntity.getChatId(), e);
                    // 出错时也保存已收集的内容
                    if (responseCollector.length() > 0) {
                        chatEntity.setContent(responseCollector.toString());
                        agentMapper.saveChat(chatEntity);
                    }
                });
    }
}
