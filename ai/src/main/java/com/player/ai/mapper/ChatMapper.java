package com.player.ai.mapper;

import com.player.ai.entity.ChatEntity;
import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ChatModelEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId,int start,int limit);

    Long getChatHistoryTotal(String userId);

    List<ChatModelEntity> getModelList();

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId);
}
