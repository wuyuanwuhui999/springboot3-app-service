package com.player.agent.service;

import com.player.common.entity.ResultEntity;

public interface IAgentService {
    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

}
