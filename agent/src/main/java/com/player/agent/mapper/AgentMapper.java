package com.player.agent.mapper;

import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ChatEntity;
import com.player.common.entity.ChatModelEntity;
import com.player.common.entity.MusicEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId, int start, int limit);

    Long getChatHistoryTotal(String userId);

    List<ChatModelEntity> getModelList();

    List<MusicEntity>selectMusicList(String auther);

    ChatModelEntity getModelById(String modelId);

}
