package com.player.prompt.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.prompt.entity.PromptEntity;
import com.player.prompt.entity.UserPromptEntity;
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
    public ResultEntity getPrompt(String userId,String tenantId,String promptId) {
        PromptEntity promptEntity = promptMapper.getPrompt(userId,tenantId,promptId);
        if(promptEntity == null){
            promptEntity = new PromptEntity();
            promptEntity.setTenantId(tenantId);
            promptEntity.setUserId(userId);
            promptEntity.setId(UUID.randomUUID().toString().replace("-", ""));
            promptEntity.setPrompt("你叫小吴同学，是一个无所不能的AI助手，上知天文下知地理，请用小吴同学的身份回答问题。");
            promptMapper.insertPrompt(promptEntity);
        }
        return ResultUtil.success(promptEntity);
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

            promptEntity.setUserId(userId);
            promptEntity.setUpdateTime(new Date());

            promptMapper.updatePrompt(promptEntity);
            return ResultUtil.success("更新提示词成功");
        } catch (Exception e) {
            return ResultUtil.fail("更新提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity insertPrompt(PromptEntity promptEntity, String userId) {
        try {

            promptEntity.setUserId(userId);
            promptEntity.setUpdateTime(new Date());
            promptMapper.insertPrompt(promptEntity);
            return ResultUtil.success("插入提示词成功");
        } catch (Exception e) {
            return ResultUtil.fail("插入提示词异常：" + e.getMessage());
        }
    }

    @Override
    public ResultEntity getPromptList(String userId,String tenantId, String keyword,int pageSize,int pageNum) {
        try {
            int offset = (pageNum - 1) * pageSize;
            List<PromptEntity> promptList = promptMapper.getPromptList(tenantId, userId, keyword,pageSize,offset);
            promptMapper.getPromptListCount(tenantId, userId, keyword);
            return ResultUtil.success(promptList, "查询提示词列表成功");
        } catch (Exception e) {
            return ResultUtil.fail("查询提示词列表异常：" + e.getMessage());
        }
    }
}