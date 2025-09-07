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

    // 查询用户信息
    @GetMapping("/getTenantUsers")
    public ResultEntity getTenantUsers(
            @RequestHeader(required = false,value = "Authorization") String token,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return tenantService.getTenantUsers(JwtToken.getId(token,secret),pageNum,pageSize);
    }
}
