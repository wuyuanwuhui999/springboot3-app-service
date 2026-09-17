package com.player.chat.tool;

import com.player.chat.mapper.ChatMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.UUID;

/**
 * 聊天工具集（租户 / 公司成员管理）
 *
 * <p>安全设计要点：</p>
 * <ul>
 *   <li>操作者（当前登录用户）、当前租户 tenantId、当前公司 companyId 均在构造时从请求上下文注入，
 *       绝不作为工具参数暴露给大模型，避免模型伪造操作者身份进行越权。</li>
 *   <li>大模型只能决定“目标用户是谁”（targetUser），且目标用户由 user_account / username / id 精确解析为 userId。</li>
 *   <li>每项写操作在工具方法里先做 Java 层权限校验（返回清晰中文提示），再执行带权限条件约束的 SQL（双重防越权）。</li>
 * </ul>
 */
public class ChatTool {

    private final ChatMapper chatMapper;
    private final String operatorUserId;
    private final String tenantId;
    private final String companyId;

    public ChatTool(ChatMapper chatMapper, String operatorUserId, String tenantId, String companyId) {
        this.chatMapper = chatMapper;
        this.operatorUserId = operatorUserId;
        this.tenantId = tenantId;
        this.companyId = companyId;
    }

    /**
     * 1. 设置指定用户为当前租户的管理员（role=1）。需要超级管理员权限（role=2）。
     */
    @Tool("把指定用户设置为当前租户的管理员。仅当前租户的超级管理员可以执行。")
    public String setTenantAdmin(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            if (chatMapper.checkTenantSuperAdmin(tenantId, operatorUserId) == 0) {
                return "无权限：设置租户管理员需要超级管理员权限";
            }
            int rows = chatMapper.setTenantUserRole(tenantId, targetUserId, operatorUserId, 1);
            if (rows > 0) return "已成功将该用户设置为当前租户的管理员";
            return "设置失败：目标用户可能不在当前租户中或已被禁用";
        } catch (Exception e) {
            return "设置租户管理员失败：" + e.getMessage();
        }
    }

    /**
     * 2. 添加用户到当前租户（普通成员，role=0）。需要普通管理员权限（role>=1）。
     */
    @Tool("把指定用户添加到当前租户（作为普通成员）。需要普通管理员权限。")
    public String addUserToTenant(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            if (chatMapper.checkTenantAdmin(tenantId, operatorUserId) == 0) {
                return "无权限：添加租户用户需要普通管理员权限";
            }
            Integer existingRole = chatMapper.getTenantUserRole(tenantId, targetUserId);
            if (existingRole != null) {
                return "该用户已在当前租户中";
            }
            String id = UUID.randomUUID().toString().replace("-", "");
            int rows = chatMapper.addTenantUser(id, tenantId, targetUserId, operatorUserId);
            if (rows > 0) return "已成功将该用户添加到当前租户";
            return "添加失败：请检查目标用户状态";
        } catch (Exception e) {
            return "添加租户用户失败：" + e.getMessage();
        }
    }

    /**
     * 3. 添加用户到当前租户，并设置为管理员。
     *    添加本身需要普通管理员权限，设置管理员需要超级管理员权限，因此整体需要超级管理员。
     */
    @Tool("把指定用户添加到当前租户，并将其设置为管理员。添加需要普通管理员权限、设置管理员需要超级管理员权限，因此该操作需要超级管理员。")
    public String addUserToTenantAsAdmin(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            if (chatMapper.checkTenantSuperAdmin(tenantId, operatorUserId) == 0) {
                return "无权限：添加用户并设置为管理员需要超级管理员权限";
            }
            // 已在该租户中则跳过添加，直接设置管理员
            Integer existingRole = chatMapper.getTenantUserRole(tenantId, targetUserId);
            if (existingRole == null) {
                String id = UUID.randomUUID().toString().replace("-", "");
                int addRows = chatMapper.addTenantUser(id, tenantId, targetUserId, operatorUserId);
                if (addRows <= 0) return "添加用户到租户失败";
            }
            int rows = chatMapper.setTenantUserRole(tenantId, targetUserId, operatorUserId, 1);
            if (rows > 0) return "已成功添加用户到当前租户并设置为管理员";
            return "设置管理员失败，请检查目标用户状态";
        } catch (Exception e) {
            return "添加用户并设置管理员失败：" + e.getMessage();
        }
    }

    /**
     * 3. 去掉指定用户在当前租户的管理员权限（role 设为 0）。需要超级管理员权限。
     */
    @Tool("取消指定用户在当前租户的管理员权限。仅当前租户的超级管理员可以执行。")
    public String cancelTenantAdmin(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            if (chatMapper.checkTenantSuperAdmin(tenantId, operatorUserId) == 0) {
                return "无权限：取消租户管理员需要超级管理员权限";
            }
            int rows = chatMapper.setTenantUserRole(tenantId, targetUserId, operatorUserId, 0);
            if (rows > 0) return "已成功取消该用户的租户管理员权限";
            return "取消失败：目标用户可能不在当前租户中或已被禁用";
        } catch (Exception e) {
            return "取消租户管理员失败：" + e.getMessage();
        }
    }

    /**
     * 4. 把指定用户移出当前租户。需要普通管理员权限（role>=1）。
     */
    @Tool("把指定用户移出当前租户。需要普通管理员权限。")
    public String removeUserFromTenant(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            if (chatMapper.checkTenantAdmin(tenantId, operatorUserId) == 0) {
                return "无权限：移除租户用户需要普通管理员权限";
            }
            if (operatorUserId.equals(targetUserId)) {
                return "不能移除自己";
            }
            int rows = chatMapper.deleteTenantUser(tenantId, targetUserId, operatorUserId);
            if (rows > 0) return "已成功将该用户移出当前租户";
            return "移除失败：用户不在当前租户中或无权操作";
        } catch (Exception e) {
            return "移除租户用户失败：" + e.getMessage();
        }
    }

    /**
     * 5. 查询当前租户内有多少人。需要是租户成员。
     */
    @Tool("查询当前租户内有多少名成员。")
    public String countTenantUsers() {
        try {
            if (isEmpty(tenantId)) return "当前会话缺少租户上下文，无法执行租户操作";
            if (chatMapper.checkTenantMember(tenantId, operatorUserId) == 0) {
                return "无权限：你不是当前租户的成员";
            }
            Long count = chatMapper.countTenantUsers(tenantId);
            return "当前租户内共有 " + (count == null ? 0 : count) + " 名成员";
        } catch (Exception e) {
            return "查询租户人数失败：" + e.getMessage();
        }
    }

    /**
     * 6. 查询当前公司内有多少员工。需要是公司成员。
     */
    @Tool("查询当前公司内有多少名员工。")
    public String countCompanyEmployees() {
        try {
            if (isEmpty(companyId)) return "当前会话缺少公司上下文，无法执行公司操作";
            Integer role = chatMapper.getCompanyUserRole(operatorUserId, companyId);
            if (role == null) return "无权限：你不是当前公司的成员";
            Long count = chatMapper.countCompanyEmployees(companyId);
            return "当前公司内共有 " + (count == null ? 0 : count) + " 名员工";
        } catch (Exception e) {
            return "查询公司人数失败：" + e.getMessage();
        }
    }

    /**
     * 7. 把指定用户移出当前公司。需要公司管理员权限（role>=1），且不能移除角色高于或等于自己的用户。
     */
    @Tool("把指定用户移出当前公司。需要公司管理员权限，且不能移除自己或角色不低于自己的用户。")
    public String removeUserFromCompany(
            @P(description = "目标用户标识，可以是用户ID、用户账号(user_account)或用户名") String targetUser
    ) {
        try {
            if (isEmpty(companyId)) return "当前会话缺少公司上下文，无法执行公司操作";
            String targetUserId = resolveUser(targetUser);
            if (targetUserId == null) return "未找到目标用户或用户标识不唯一，请用户提供更精确的用户ID、账号或用户名";
            Integer operatorRole = chatMapper.getCompanyUserRole(operatorUserId, companyId);
            if (operatorRole == null || operatorRole < 1) {
                return "无权限：移除公司员工需要公司管理员权限";
            }
            if (operatorUserId.equals(targetUserId)) {
                return "不能移除自己";
            }
            Integer targetRole = chatMapper.getCompanyUserRole(targetUserId, companyId);
            if (targetRole == null) {
                return "目标用户不在当前公司中";
            }
            // 超级管理员(2)可移除任何人；管理员(1)不能移除角色不低于自己的用户
            if (operatorRole < 2 && targetRole >= operatorRole) {
                return "无权限：不能移除角色高于或等于自己的用户";
            }
            int rows = chatMapper.deleteCompanyUser(companyId, targetUserId, operatorUserId);
            if (rows > 0) return "已成功将该用户移出当前公司";
            return "移除失败：请检查目标用户状态";
        } catch (Exception e) {
            return "移除公司员工失败：" + e.getMessage();
        }
    }

    /**
     * 把用户标识（ID / user_account / username）精确解析为 userId。
     * 未找到或匹配不唯一时返回 null。
     */
    private String resolveUser(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        List<String> ids = chatMapper.getUserIdsByIdentifier(identifier.trim());
        if (ids == null || ids.isEmpty() || ids.size() > 1) {
            return null;
        }
        return ids.get(0);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
