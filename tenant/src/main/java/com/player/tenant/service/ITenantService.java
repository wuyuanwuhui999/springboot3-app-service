package com.player.tenant.service;

import com.player.common.entity.ResultEntity;

public interface ITenantService {
    ResultEntity getUserTenantList(String userId);

    ResultEntity getTenantUsers(String tenantId, int pageNum, int pageSize);
}
