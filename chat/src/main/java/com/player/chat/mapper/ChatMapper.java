// chat/src/main/java/com/player/chat/mapper/ChatMapper.java
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

    List<ChatEntity> getChatHistory(String tenantId, String userId, int start, int limit);

    Long getChatHistoryTotal(String tenantId, String userId);

    List<ChatEntity> getChatHistoryByChatId(String userId, String chatId);

    List<ChatModelEntity> getModelList(String companyId, String keyword);

    ChatModelEntity getModelById(String companyId, String modelId);

    ChatModelEntity getModelByType(String modelType);

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId, String tenantId);

    ChatDocEntity getDocById(String docId, String userId);

    long deleteDoc(String docId, String userId);

    List<DirectoryEntity> getDirectoryList(String userId, String tenantId);

    long isDirExist(String userId, String directory);

    long createDir(DirectoryEntity directoryEntity);

    DirectoryEntity getDirectoryById(String id, String userId);

    long renameDir(DirectoryEntity directoryEntity);

    long deleteDir(String userId, long directoryId);

    List<ChatDocEntity> getDocListByDirId(String userId, String tenantId, String directoryId);

    /**
     * 插入模型
     */
    int insertModel(ChatModelEntity chatModelEntity);

    /**
     * 更新模型
     */
    int updateModel(ChatModelEntity chatModelEntity);

    /**
     * 删除模型（逻辑删除，将disabled设为1）
     */
    int deleteModel(String id, String companyId);

    /**
     * 根据ID查询模型（用于权限校验）
     */
    ChatModelEntity getModelByIdForAuth(String id, String companyId);

    /**
     * 查询用户在指定公司的角色
     * @param userId 用户ID
     * @param companyId 公司ID
     * @return 角色值：2-超级管理员，1-管理员，0-普通成员，null表示不在该公司
     */
    Integer getCompanyUserRole(String userId, String companyId);
}