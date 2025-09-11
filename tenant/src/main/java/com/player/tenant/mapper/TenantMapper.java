package com.player.tenant.mapper;

import com.player.tenant.entity.TenantEntity;
import com.player.tenant.entity.TenantUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantMapper {
    List<TenantEntity>getUserTenantList(String userId);

    List<TenantUserEntity> getTenantUserList(String tenantId, String userId, int offset, int pageSize);

    Long getTenantUserListCount(String tenantId);

    TenantUserEntity getTenantUser(String tenantId, String userId);

    int setAdmin(String tenantId,String userId,String adminUserId,int roleType);
}
