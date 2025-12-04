package com.player.gateway.music.mapper;

import com.player.gateway.common.entity.ChatDocEntity;
import com.player.gateway.common.entity.ChatEntity;
import com.player.gateway.common.entity.ChatModelEntity;
import com.player.gateway.music.entity.MusicEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId, int start, int limit);

    Long getChatHistoryTotal(String userId);

    List<MusicEntity> selectMusicList(String name);

    List<ChatModelEntity> getModelList();

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId);

    ChatDocEntity getDocById(String docId, String userId);

    long deleteDoc(String docId, String userId);
}
