package com.player.user.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.UserEntity;
import com.player.user.entity.MailEntity;
import com.player.user.entity.PasswordEntity;
import com.player.user.entity.ResetPasswordEntity;
import com.player.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/service")
@RestController
public class UserController {
    @Autowired
    private IUserService userService;

    // 查询用户信息
    @GetMapping("/user/getUserData")
    public ResultEntity getUserData(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return userService.getUserData(userId);
    }

    // 登录校验
    @PostMapping("/user/login")
    public ResultEntity login(@RequestBody UserEntity userEntity) {
        return userService.login(userEntity);
    }

    // 注册
    @PostMapping("/user/register")
    public ResultEntity register(@RequestBody UserEntity userEntity) {
        return userService.register(userEntity);
    }

    // 查询用户是否存在
    @PostMapping("/user/vertifyUser")
    public ResultEntity vertifyUser(@RequestBody UserEntity userEntity) {
        return userService.vertifyUser(userEntity);
    }

    // 更新用户信息
    @PutMapping("/user/updateUser")
    public ResultEntity updateUser(@RequestHeader("X-User-Id") String userId, @RequestBody UserEntity userEntity) {
        return userService.updateUser(userEntity, userId);
    }

    // 修改密码
    @PutMapping("/user/updatePassword")
    public ResultEntity updatePassword(@RequestHeader("X-User-Id") String userId, @RequestBody PasswordEntity passwordEntity) {
        return userService.updatePassword(passwordEntity, userId);
    }

    // 头像上传
    @PutMapping("/user/updateAvater")
    public ResultEntity updateAvater(@RequestHeader("X-User-Id") String userId, @RequestBody Map map) {
        return userService.updateAvater(userId, map.get("img").toString());
    }

    // 找回密码
    @PostMapping("/user/sendEmailVertifyCode")
    public ResultEntity sendEmailVertifyCode(@RequestBody MailEntity mailRequest) {
        return userService.sendEmailVertifyCode(mailRequest);
    }

    // 更新密码
    @PostMapping("/user/resetPassword")
    public ResultEntity resetPassword(@RequestBody ResetPasswordEntity resetPasswordEntity) {
        return userService.resetPassword(resetPasswordEntity);
    }

    // 邮箱登录
    @PostMapping("/user/loginByEmail")
    public ResultEntity loginByEmail(@RequestBody MailEntity mailEntity) {
        return userService.loginByEmail(mailEntity);
    }

    // 搜索用户
    @GetMapping("/user/searchUsers")
    public ResultEntity searchUsers(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "tenantId") String tenantId
    ) {
        return userService.searchUsers(keyword, tenantId);
    }
}