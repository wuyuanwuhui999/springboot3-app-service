package com.player.tenant.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.tenant.entity.TenantUserEntity;
import com.player.tenant.mapper.TenantMapper;
import com.player.tenant.service.ITenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
