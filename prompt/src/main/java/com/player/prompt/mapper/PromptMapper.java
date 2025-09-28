package com.player.prompt.mapper;

import com.player.prompt.entity.PromptEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptMapper {

    // 新增提示词
    int insertPrompt(PromptEntity promptEntity);

    // 根据ID、租户ID和创建人ID删除提示词
    int deletePrompt(String id,String tenantId, String createdBy);

    // 更新提示词（根据ID、租户ID和创建人ID过滤）
    int updatePrompt(PromptEntity promptEntity);

    // 根据ID、租户ID和创建人ID查询提示词
    PromptEntity getPromptById(String id, String tenantId,String createdBy);

    // 查询提示词列表（支持模糊查询，按创建时间降序排序）
    List<PromptEntity> getPromptList(String tenantId, String createdBy, String content, String industry, String tags);
}