package com.player.tenant.service;

import com.player.common.entity.ResultEntity;

public interface ITenantService {
    ResultEntity getUserTenantList(String userId);

    ResultEntity getTenantUserList(String tenantId,String userId, int pageNum, int pageSize);

    ResultEntity getTenantUser(String tenantId, String userId);

    ResultEntity setAdmin(String tenantId, String userId,String adminUserId,int roleType);
}

