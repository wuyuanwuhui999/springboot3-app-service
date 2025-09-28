package com.player.prompt.mapper;

import com.player.prompt.entity.TenantEntity;
import com.player.prompt.entity.TenantUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantMapper {
    List<TenantEntity>getUserTenantList(String userId);

    List<TenantUserEntity> getTenantUserList(String tenantId, String userId, int offset, int pageSize);

    Long getTenantUserListCount(String tenantId);

    TenantUserEntity getTenantUser(String tenantId, String userId);

    int setAdmin(String tenantId,String userId,String adminUserId,int roleType);

    // 新增的方法
    int addTenantUser(String id,String tenantId, String userId,String adminUserId);

    // 新增的方法
    int deleteTenantUser(String tenantId,String userId, String adminUserId);

    // 检查用户是否有管理员权限
    int checkAdminPermission(String tenantId, String userId);
}
