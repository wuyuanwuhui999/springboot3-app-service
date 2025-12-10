package com.player.agent.service;

import com.player.common.entity.ResultEntity;
import com.player.agent.entity.AgentParamsEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

public interface IAgentService {
    Flux<String> chat(String userId, AgentParamsEntity agentParamsEntity);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    ResultEntity uploadDoc(MultipartFile file,String userId) throws IOException;

    ResultEntity getDocList(String userId);

    ResultEntity deleteDoc(String docId, String userId);
}
