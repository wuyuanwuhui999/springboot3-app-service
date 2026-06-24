package com.player.tenant.service;

import com.player.common.entity.ResultEntity;

public interface ITenantService {
    ResultEntity getTenantList(String userId,String companyId);

    ResultEntity getTenantUserList(String tenantId,String userId, String keyword, int pageNum, int pageSize);

    ResultEntity searchUsers(String tenantId,String userId, String keyword, int pageNum, int pageSize);

    ResultEntity getTenantUser(String tenantId, String userId);

    ResultEntity setAdmin(String tenantId, String userId,String adminUserId,int role);

    ResultEntity addTenantUser(String tenantId, String userId,String adminUserId);

    ResultEntity deleteTenantUser(String tenantId, String userId,String adminUserId);
}

