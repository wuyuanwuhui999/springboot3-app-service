package com.player.ai.mapper;

import com.player.ai.entity.ChatEntity;
import com.player.ai.entity.DirectoryEntity;
import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ChatModelEntity;
import com.player.common.entity.ResultEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId,int start,int limit);

    Long getChatHistoryTotal(String userId);

    List<ChatModelEntity> getModelList();

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId,String appId);

    ChatDocEntity getDocById(String docId, String userId,String directoryId);

    long deleteDoc(String docId, String userId, String directoryId);

    List<DirectoryEntity> getDirectoryList(String userId);

    long isDirExist(String userId, String directory);

    long createDir(DirectoryEntity directoryEntity);

    long renameDir(DirectoryEntity directoryEntity);

    long deleteDir(String userId, long directoryId);
}
