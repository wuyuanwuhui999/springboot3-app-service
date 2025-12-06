package com.player.tenant.controller;

import com.player.common.entity.ResultEntity;
import com.player.tenant.service.ITenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service/tenant")
@RestController
public class TenantController {

    @Autowired
    private ITenantService tenantService;

    // 查询用户信息
    @GetMapping("/getUserTenantList")
    public ResultEntity getUserTenantList(@RequestHeader("X-User-Id") String userId) {
        return tenantService.getUserTenantList(userId);
    }

    // 查询当前租户下的用户列表
    @GetMapping("/getTenantUserList")
    public ResultEntity getTenantUserList(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("tenantId") String tenantId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ) {
        return tenantService.getTenantUserList(tenantId,userId,pageNum,pageSize);
    }

    // 查询当前租户的用户信息
    @GetMapping("/getTenantUser")
    public ResultEntity getTenantUser(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("tenantId") String tenantId
    ) {
        return tenantService.getTenantUser(tenantId,userId);
    }

    // 添加用户为管理员
    @PutMapping("/addAdmin/{tenantId}/{userId}")
    public ResultEntity addAdmin(
            @RequestHeader("X-User-Id") String id,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.setAdmin(tenantId,userId,id,1);
    }

    // 取消用户为管理员
    @PutMapping("/cancelAdmin/{tenantId}/{userId}")
    public ResultEntity cancelAdmin(
            @RequestHeader("X-User-Id") String id,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.setAdmin(tenantId,userId,id,0);
    }

    // 取消用户为管理员
    @PostMapping("/addTenantUser/{tenantId}/{userId}")
    public ResultEntity addTenantUser(
            @RequestHeader("X-User-Id") String id,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.addTenantUser(tenantId,userId,userId);
    }

    // 取消用户为管理员
    @DeleteMapping("/deleteTenantUser/{tenantId}/{userId}")
    public ResultEntity deleteTenantUser(
            @RequestHeader("X-User-Id") String id,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.deleteTenantUser(tenantId,userId,id);
    }
}
