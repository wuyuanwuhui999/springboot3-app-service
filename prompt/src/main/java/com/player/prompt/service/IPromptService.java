package com.player.prompt.service;

import com.player.common.entity.ResultEntity;
import com.player.prompt.entity.UserPromptEntity;

public interface IPromptService {

    // 新增提示词
    ResultEntity insertPrompt(UserPromptEntity userPromptEntity, String userId);

    // 删除提示词
    ResultEntity deletePrompt(String id, String userId, String tenantId);

    // 更新提示词
    ResultEntity updatePrompt(UserPromptEntity userPromptEntity, String userId);

    // 根据ID查询提示词
    ResultEntity getPromptById(String id, String userId, String tenantId);

    // 查询提示词列表
    ResultEntity getPromptList(String userId, String tenantId, String content, String industry, String tags);

    ResultEntity getPromptCategoryList();

    ResultEntity getSystemPromptListByCategory(String categoryId,String keyword,String userId,int pageNum,int pageSize);
}
