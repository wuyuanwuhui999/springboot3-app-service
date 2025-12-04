package com.player.user.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.UserEntity;
import com.player.user.entity.MailEntity;
import com.player.user.entity.PasswordEntity;
import com.player.user.entity.ResetPasswordEntity;
import com.player.user.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/service")
@RestController
public class UserController {
    @Autowired
    private IUserService userService;

    // 查询用户信息
    @GetMapping("/user-getway/getUserData")
    public ResultEntity getUserData(@RequestHeader(required = false,value = "Authorization") String token) {
        return userService.getUserData(token);
    }

    // 登录校验
    @PostMapping("/user/login")
    public ResultEntity login(@RequestBody UserEntity userEntity) {
        return userService.login(userEntity);
    }

    // 退出登录
    @PostMapping("/user-getway/logout")
    public ResultEntity logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
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
    @PutMapping("/user-getway/updateUser")
    public ResultEntity updateUser(@RequestHeader("Authorization") String token,@RequestBody UserEntity userEntity,HttpServletRequest request) {
        return userService.updateUser(userEntity,token);
    }

    // 修改密码
    @PutMapping("/user-getway/updatePassword")
    public ResultEntity updatePassword(@RequestHeader("Authorization") String token,@RequestBody PasswordEntity passwordEntity,HttpServletRequest request) {
        return userService.updatePassword(passwordEntity,token);
    }

    // 头像上传
    @PutMapping("/user-getway/updateAvater")
    public ResultEntity updateAvater(@RequestHeader("Authorization") String token, @RequestBody Map map) {
        return userService.updateAvater(token,map.get("img").toString());
    }

    // 找回密码
    @PostMapping("/user/sendEmailVertifyCode")
    public ResultEntity sendEmailVertifyCode(@RequestBody MailEntity mailRequest ) {
        return userService.sendEmailVertifyCode(mailRequest);
    }

    // 更新密码
    @PostMapping("/user/resetPassword")
    public ResultEntity resetPassword(@RequestBody ResetPasswordEntity resetPasswordEntity ) {
        return userService.resetPassword(resetPasswordEntity);
    }

    // 邮箱登录
    @PostMapping("/user/loginByEmail")
    public ResultEntity loginByEmail(@RequestBody MailEntity mailEntity ) {
        return userService.loginByEmail(mailEntity);
    }

    // 搜索用户
    @GetMapping("/user-getway/searchUsers")
    public ResultEntity searchUsers(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "tenantId") String tenantId
    ) {
        return userService.searchUsers(keyword,tenantId);
    }
}
