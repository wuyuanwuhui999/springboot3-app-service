package com.player.gateway.tenant.controller;

import com.player.gateway.common.entity.ResultEntity;
import com.player.gateway.common.utils.JwtToken;
import com.player.gateway.tenant.service.ITenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service/tenant")
@RestController
public class TenantController {
    @Value("${token.secret}")
    private String secret;

    @Autowired
    private ITenantService tenantService;

    // 查询用户信息
    @GetMapping("/getUserTenantList")
    public ResultEntity getUserTenantList(@RequestHeader(required = false,value = "Authorization") String token) {
        return tenantService.getUserTenantList(JwtToken.getId(token,secret));
    }

    // 查询当前租户下的用户列表
    @GetMapping("/getTenantUserList")
    public ResultEntity getTenantUserList(
            @RequestHeader(required = false,value = "Authorization") String token,
            @RequestParam("tenantId") String tenantId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ) {
        return tenantService.getTenantUserList(tenantId,JwtToken.getId(token,secret),pageNum,pageSize);
    }

    // 查询当前租户的用户信息
    @GetMapping("/getTenantUser")
    public ResultEntity getTenantUser(
            @RequestHeader(required = false,value = "Authorization") String token,
            @RequestParam("tenantId") String tenantId
    ) {
        return tenantService.getTenantUser(tenantId,JwtToken.getId(token,secret));
    }

    // 添加用户为管理员
    @PutMapping("/addAdmin/{tenantId}/{userId}")
    public ResultEntity addAdmin(
            @RequestHeader(required = false,value = "Authorization") String token,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.setAdmin(tenantId,userId,JwtToken.getId(token,secret),1);
    }

    // 取消用户为管理员
    @PutMapping("/cancelAdmin/{tenantId}/{userId}")
    public ResultEntity cancelAdmin(
            @RequestHeader(required = false,value = "Authorization") String token,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.setAdmin(tenantId,userId,JwtToken.getId(token,secret),0);
    }

    // 取消用户为管理员
    @PostMapping("/addTenantUser/{tenantId}/{userId}")
    public ResultEntity addTenantUser(
            @RequestHeader(required = false,value = "Authorization") String token,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.addTenantUser(tenantId,userId,JwtToken.getId(token,secret));
    }

    // 取消用户为管理员
    @DeleteMapping("/deleteTenantUser/{tenantId}/{userId}")
    public ResultEntity deleteTenantUser(
            @RequestHeader(required = false,value = "Authorization") String token,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("userId") String userId
    ) {
        return tenantService.deleteTenantUser(tenantId,userId,JwtToken.getId(token,secret));
    }
}
