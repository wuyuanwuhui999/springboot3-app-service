package com.player.prompt.service;

import com.player.common.entity.ResultEntity;
import com.player.prompt.entity.PromptEntity;

public interface IPromptService {
    ResultEntity getPrompt(String userId,String tenantId,String proptId);

    // 删除提示词
    ResultEntity deletePrompt(String id, String userId, String tenantId);

    // 更新提示词
    ResultEntity updatePrompt(PromptEntity promptEntity, String userId);

    // 更新提示词
    ResultEntity insertPrompt(PromptEntity promptEntity, String userId);

    // 查询提示词列表
    ResultEntity getPromptList(String userId, String tenantId, String keyword,int pageSize,int pageNum);
}
