package com.player.chat.mapper;

import com.player.chat.entity.ChatEntity;
import com.player.chat.entity.DirectoryEntity;
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

    // 新增方法：根据模型ID查询模型配置
    ChatModelEntity getModelById(String modelId);

    // 在ChatMapper.java中添加以下方法
    ChatModelEntity getModelByType(String modelType);

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId,String tenantId);

    ChatDocEntity getDocById(String docId, String userId,String directoryId);

    long deleteDoc(String docId, String userId, String directoryId);

    List<DirectoryEntity> getDirectoryList(String userId,String tenantId);

    long isDirExist(String userId, String directory);

    long createDir(DirectoryEntity directoryEntity);

    DirectoryEntity getDirectoryById(String id, String userId);

    long renameDir(DirectoryEntity directoryEntity);

    long deleteDir(String userId, long directoryId);

    List<ChatDocEntity>getDocListByDirId(String userId,String tenantId,String directoryId);
}
