package com.player.user.service;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.UserEntity;
import com.player.user.entity.MailEntity;
import com.player.user.entity.PasswordEntity;
import com.player.user.entity.ResetPasswordEntity;

public interface IUserService {
    ResultEntity getUserData(String userId);  // 参数改为userId

    ResultEntity login(UserEntity userEntity);

    ResultEntity register(UserEntity userEntity);

    ResultEntity vertifyUser(UserEntity userEntity);

    ResultEntity updateUser(UserEntity userEntity, String userId);  // 参数改为userId

    ResultEntity updatePassword(PasswordEntity passwordEntity, String userId);  // 参数改为userId

    ResultEntity updateAvater(String userId, String base64);  // 参数改为userId

    ResultEntity resetPassword(ResetPasswordEntity resetPasswordEntity);

    ResultEntity sendEmailVertifyCode(MailEntity mailEntity);

    ResultEntity loginByEmail(MailEntity mailEntity);

    ResultEntity searchUsers(String keyword, String tenantId);
}