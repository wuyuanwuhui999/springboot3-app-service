package com.player.prompt.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.prompt.entity.TenantUserEntity;
import com.player.prompt.mapper.TenantMapper;
import com.player.prompt.service.ITenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService implements ITenantService {
    @Autowired
    private TenantMapper tenantMapper;

    @Override
    public ResultEntity getUserTenantList(String userId) {
        return ResultUtil.success(tenantMapper.getUserTenantList(userId));
    }

    @Override
    public ResultEntity getTenantUserList(String tenantId, String userId,int pageNum, int pageSize) {
        // 计算分页参数
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<TenantUserEntity> users = tenantMapper.getTenantUserList(tenantId, userId, offset, pageSize);
        Long total = tenantMapper.getTenantUserListCount(tenantId);
        ResultEntity resultEntity = ResultUtil.success(users);
        resultEntity.setTotal(total);
        return resultEntity;
    }

    @Override
    public ResultEntity getTenantUser(String tenantId, String userId) {
        return ResultUtil.success(tenantMapper.getTenantUser(tenantId,userId));
    }

    @Override
    public ResultEntity setAdmin(String tenantId, String userId, String adminUserId, int roleType) {
        return ResultUtil.success(tenantMapper.setAdmin(tenantId,userId,adminUserId,roleType));
    }

    @Override
    public ResultEntity addTenantUser(String tenantId, String userId, String adminUserId) {
        // 检查操作者是否有管理员权限
        int hasPermission = tenantMapper.checkAdminPermission(tenantId, adminUserId);
        if (hasPermission == 0) {
            return ResultUtil.fail("无权限操作，需要管理员或超级管理员权限");
        }

        // 检查用户是否已经在租户中
        TenantUserEntity existingUser = tenantMapper.getTenantUser(tenantId, userId);
        if (existingUser != null) {
            return ResultUtil.fail("用户已在该租户中");
        }

        // 生成唯一ID并添加用户
        String id = UUID.randomUUID().toString().replace("-", "");
        int result = tenantMapper.addTenantUser(id, tenantId, userId, adminUserId);

        if (result > 0) {
            // 返回新添加的用户信息
            TenantUserEntity newUser = tenantMapper.getTenantUser(tenantId, userId);
            return ResultUtil.success(newUser);
        } else {
            return ResultUtil.fail("添加用户失败");
        }
    }

    @Override
    public ResultEntity deleteTenantUser(String tenantId, String userId, String adminUserId) {
        // 检查操作者是否有管理员权限
        int hasPermission = tenantMapper.checkAdminPermission(tenantId, adminUserId);
        if (hasPermission == 0) {
            return ResultUtil.fail(false,"无权限操作，需要管理员或超级管理员权限");
        }

        // 检查要删除的用户是否存在
        TenantUserEntity existingUser = tenantMapper.getTenantUser(tenantId, userId);
        if (existingUser == null) {
            return ResultUtil.fail(false,"用户不在该租户中");
        }

        // 不能删除自己
        if (userId.equals(adminUserId)) {
            return ResultUtil.fail(false,"不能删除自己");
        }

        int result = tenantMapper.deleteTenantUser(tenantId, userId, adminUserId);

        if (result > 0) {
            return ResultUtil.success(true,"删除用户成功");
        } else {
            return ResultUtil.fail(false,"删除用户失败");
        }
    }
}
