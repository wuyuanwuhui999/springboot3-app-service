package com.player.prompt.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.utils.JwtToken;
import com.player.prompt.entity.PromptEntity;
import com.player.prompt.mapper.PromptMapper;
import com.player.prompt.service.IPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PromptService implements IPromptService {
    @Autowired
    private PromptMapper promptMapper;

    @Override
    public ResultEntity insertPrompt(PromptEntity promptEntity, String userId) {
        try {

            promptEntity.setId(UUID.randomUUID().toString());
            promptEntity.setCreatedBy(userId);
            promptEntity.setUpdatedBy(userId);
            promptEntity.setCreateDate(new Date());
            promptEntity.setUpdateDate(new Date());

            int result = promptMapper.insertPrompt(promptEntity);
            if (result > 0) {
                return ResultUtil.success("新增提示词成功");
            } else {
                return ResultUtil.fail("新增提示词失败");
            }
        } catch (Exception e) {
            return ResultUtil.fail("新增提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity deletePrompt(String id, String userId,String tenantId) {
        try {

            int result = promptMapper.deletePrompt(id, tenantId, userId);
            if (result > 0) {
                return ResultUtil.success("删除提示词成功");
            } else {
                return ResultUtil.fail("删除提示词失败，可能不存在或无权操作");
            }
        } catch (Exception e) {
            return ResultUtil.fail("删除提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity updatePrompt(PromptEntity promptEntity, String userId) {
        try {

            promptEntity.setUpdatedBy(userId);
            promptEntity.setUpdateDate(new Date());

            int result = promptMapper.updatePrompt(promptEntity);
            if (result > 0) {
                return ResultUtil.success("更新提示词成功");
            } else {
                return ResultUtil.fail("更新提示词失败，可能不存在或无权操作");
            }
        } catch (Exception e) {
            return ResultUtil.fail("更新提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity getPromptById(String id, String userId,String tenantId) {
        try {
            PromptEntity prompt = promptMapper.getPromptById(id, tenantId, userId);
            if (prompt != null) {
                return ResultUtil.success(prompt, "查询提示词成功");
            } else {
                return ResultUtil.fail("提示词不存在或无权查看");
            }
        } catch (Exception e) {
            return ResultUtil.fail("查询提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity getPromptList(String userId,String tenantId, String content, String industry, String tags) {
        try {
            List<PromptEntity> promptList = promptMapper.getPromptList(tenantId, userId, content, industry, tags);
            return ResultUtil.success(promptList, "查询提示词列表成功");
        } catch (Exception e) {
            return ResultUtil.fail("查询提示词列表异常：" + e.getMessage());
        }
    }
}