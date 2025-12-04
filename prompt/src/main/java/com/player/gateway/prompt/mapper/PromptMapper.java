package com.player.gateway.prompt.mapper;

import com.player.gateway.prompt.entity.PromptCategoryEntity;
import com.player.gateway.prompt.entity.SystemPromptEntity;
import com.player.gateway.prompt.entity.UserPromptEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptMapper {

    // 新增提示词
    int insertPrompt(UserPromptEntity userPromptEntity);

    // 根据ID、租户ID和创建人ID删除提示词
    int deletePrompt(String id,String tenantId, String createdBy);

    // 更新提示词（根据ID、租户ID和创建人ID过滤）
    int updatePrompt(UserPromptEntity userPromptEntity);

    // 根据ID、租户ID和创建人ID查询提示词
    UserPromptEntity getPromptById(String id, String tenantId, String createdBy);

    // 查询提示词列表（支持模糊查询，按创建时间降序排序）
    List<UserPromptEntity> getPromptList(String tenantId, String createdBy, String content, String categoryId, String tags);

    List<PromptCategoryEntity>getPromptCategoryList();

    List<SystemPromptEntity>getSystemPromptListByCategory(String categoryId, String keyword,String userId, int start, int pageSize);

    Long getSystemPromptCountByCategory(String categoryId, String keyword);

    Long insertCollectPrompt(String promptId, String userId);

    Long deleteCollectPrompt(String promptId, String userId);

    List<PromptCategoryEntity> getMyCollectPromptCategory(String tenantId,String userId);

    List<SystemPromptEntity> getMyCollectPromptList(String tenantId,String categoryId,String userId, int start, int pageSize);

    Long getMyCollectPromptCount(String tenantId,String categoryId, String userId);
}