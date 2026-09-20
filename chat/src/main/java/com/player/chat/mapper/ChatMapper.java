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

    /**
     * 根据 userId、tenantId、promptId 查询提示词内容（chat WebSocket 使用）
     */
    String getPrompt(String userId, String tenantId, String promptId);

    /**
     * 根据租户ID查询所属公司ID（文档向量检索「公司内公开」过滤使用）
     */
    String getCompanyIdByTenantId(String tenantId);

    ChatModelEntity getModelByType(String modelType);

    void saveDoc(ChatDocEntity chatDocEntity);

    List<ChatDocEntity> getDocList(String userId, String tenantId, String permission);

    ChatDocEntity getDocById(String docId, String userId);

    long deleteDoc(String docId, String userId);

    /**
     * 更新文档权限（仅限自己的文档）
     */
    int updateDocPermission(String docId, String userId, String permission);

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

    // ==================== 工具调用（tenant/company 权限操作） ====================

    /**
     * 根据用户标识（用户ID / 用户账号 user_account / 用户名）精确匹配用户ID
     */
    List<String> getUserIdsByIdentifier(String identifier);

    /**
     * 检查操作者是否为指定租户的超级管理员（role=2）
     */
    int checkTenantSuperAdmin(String tenantId, String userId);

    /**
     * 检查操作者是否为指定租户的管理员或超级管理员（role IN (1,2)）
     */
    int checkTenantAdmin(String tenantId, String userId);

    /**
     * 检查操作者是否为指定租户的成员（未禁用）
     */
    int checkTenantMember(String tenantId, String userId);

    /**
     * 查询用户在指定租户中的角色，不在租户中返回 null
     */
    Integer getTenantUserRole(String tenantId, String userId);

    /**
     * 设置用户在租户中的角色（SQL 内部强制操作者为超级管理员 role=2，防越权）
     */
    int setTenantUserRole(String tenantId, String userId, String adminUserId, int role);

    /**
     * 添加用户到租户（SQL 内部强制操作者为管理员 role IN (1,2)，防越权）
     */
    int addTenantUser(String id, String tenantId, String userId, String adminUserId);

    /**
     * 从租户移除用户（SQL 内部强制操作者为管理员 role IN (1,2)，且不能移除自己）
     */
    int deleteTenantUser(String tenantId, String userId, String adminUserId);

    /**
     * 统计租户内的有效成员数
     */
    Long countTenantUsers(String tenantId);

    /**
     * 统计公司内的有效员工数
     */
    Long countCompanyEmployees(String companyId);

    /**
     * 从公司移除员工（SQL 内部强制操作者为管理员 role>=1，且不能移除自己）
     */
    int deleteCompanyUser(String companyId, String userId, String adminUserId);
}