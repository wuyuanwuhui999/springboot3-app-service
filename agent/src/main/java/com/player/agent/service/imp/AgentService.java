package com.player.agent.service.imp;

import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.agent.service.IAgentService;
import com.player.agent.tool.AgentTool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService implements IAgentService {

    @Autowired
    private AgentMapper agentMapper;

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
}
