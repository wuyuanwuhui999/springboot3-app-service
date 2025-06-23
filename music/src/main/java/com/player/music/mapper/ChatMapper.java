package com.player.music.mapper;

import com.player.music.entity.ChatDocEntity;
import com.player.music.entity.ChatEntity;
import com.player.music.entity.ChatModelEntity;
import com.player.music.entity.MusicEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId,int start,int limit);

    Long getChatHistoryTotal(String userId);

    List<MusicEntity> selectMusicList(String name);

    List<ChatModelEntity> getModelList();

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId);
}
