package com.player.tenant.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.utils.JwtToken;
import com.player.tenant.service.ITenantService;
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
            @RequestParam(defaultValue = "") String tenantId
    ) {
        return tenantService.getTenantUser(tenantId,JwtToken.getId(token,secret));
    }
}
