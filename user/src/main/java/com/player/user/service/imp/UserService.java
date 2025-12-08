package com.player.user.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.entity.UserEntity;
import com.player.common.utils.Common;
import com.player.common.utils.JwtToken;
import com.player.common.utils.ResultCode;
import com.player.user.entity.MailEntity;
import com.player.user.entity.PasswordEntity;
import com.player.user.entity.ResetPasswordEntity;
import com.player.user.entity.SearchUserEntity;
import com.player.user.mapper.UserMapper;
import com.player.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class UserService implements IUserService {
    @Autowired
    private RedisTemplate redisTemplate;

    //注入邮件工具类
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${token.secret}")
    public String secret;

    @Value("${app.avater-path}")
    private String avaterPath;

    @Value("${app.avater-img}")
    private String avaterImg;

    @Value("${spring.mail.username}")
    private String sendMailer;

    @Autowired
    private UserMapper userMapper;

    /**
     * @author: wuwenqiang
     * @description: 获取用户数据
     * @date: 2021-06-16 22:50
     */
    @Override
    public ResultEntity getUserData(String userId) {
        UserEntity userEntity = userMapper.getMyUserData(userId);

        // 注意：不再生成新的token，因为网关已经处理了token验证
        // 如果还需要生成token，可以保留这部分逻辑
        String newToken = null; // 如果需要生成token，可以使用原有逻辑

        if (userEntity != null) {
             newToken = JwtToken.createToken(userEntity,secret);
        }

        return ResultUtil.success(userEntity, null, newToken);
    }

    /**
     * @author: wuwenqiang
     * @description: 登录校验
     * @date: 2024-12-25 00:04
     */
    @Override
    public ResultEntity login(UserEntity userEntity) {
        // 查询redis账号是否是在黑名单内
        String isBlackList = (String)redisTemplate.opsForValue().get("black_list_" + userEntity.getUserAccount());
        // 如果在黑名单内，禁止登录，返回错误信息给用户
        if(!StringUtils.isEmpty(isBlackList))return  ResultUtil.fail(null, "该账号被禁止登录", ResultCode.FAIL);
        // 使用账号密码查询数据库
        UserEntity resultUserEntity = userMapper.login(userEntity);
        if (resultUserEntity != null) {// 如果返回用户信息，说明账号密码正确
            // 注意：登录接口仍然需要生成token，因为这是认证过程
            String token = JwtToken.createToken(resultUserEntity,secret);//生成新的token
            redisTemplate.opsForValue().set(token, "1",30, TimeUnit.DAYS);// token保存到redis中，有效期30天
            return ResultUtil.success(resultUserEntity, "登录成功", token);// 返回用户信息和token给用户
        } else {// 没有查询到用户信息登录失败，用户账号密码错误
            return ResultUtil.fail(null, "登录失败，账号或密码错误", ResultCode.FAIL);
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 注册
     * @date: 2021-01-01 23:39
     */
    @Override
    @Transactional
    public ResultEntity register(UserEntity userEntity) {
        userEntity.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        Long row = userMapper.register(userEntity);
        if (row > 0 ) {
            UserEntity userEntity1 = userMapper.queryUser(userEntity);
            // 注册成功后生成token
            String newToken = JwtToken.createToken(userEntity1,secret);
            redisTemplate.opsForValue().set(newToken, "1",30, TimeUnit.DAYS);
            return ResultUtil.success(userEntity1, null, newToken);
        }
        return ResultUtil.fail(null, "注册失败");
    }

    /**
     * @author: wuwenqiang
     * @description: 查询单个用户，用于校验用户是否存在
     * @date: 2021-06-17 22:33
     */
    @Override
    public ResultEntity vertifyUser(UserEntity userEntity) {
        UserEntity mUserEntity = userMapper.queryUser(userEntity);
        if(mUserEntity != null){
            if(mUserEntity.getUserAccount().equals(userEntity.getUserAccount())){
                return ResultUtil.success(1,"账号已存在");
            }else {
                return ResultUtil.success(1,"邮箱已存在");
            }
        }else{
            return ResultUtil.success(0);
        }
    }

    /**
     * @author: wuwenqiang
     * @description: 更新用户信息
     * @date:  2021-06-17 22:33
     */
    @Override
    @Transactional
    public ResultEntity updateUser(UserEntity userEntity, String userId) {
        // 直接从参数获取用户ID，不再解析token
        userEntity.setUserAccount(userId);
        Long result = userMapper.updateUser(userEntity);
        return ResultUtil.success(result, "更新成功");
    }

    /**
     * @author: wuwenqiang
     * @description: 修改密码
     * @date: 2020-12-24 22:40
     */
    @Override
    @Transactional
    public ResultEntity updatePassword(PasswordEntity passwordEntity, String userId) {
        // 直接从参数获取用户ID
        passwordEntity.setId(userId);
        Long row = userMapper.updatePassword(passwordEntity);
        if(row > 0){
            return ResultUtil.success(row,"修改密码成功");
        }
        return ResultUtil.fail(0,"旧密码错误");
    }

    /**
     * @author: wuwenqiang
     * @methodsName: updatePassword
     * @description: 修改密码
     * @return: ResultEntity
     * @date: 2021-06-18 00:21
     */
    @Override
    @Transactional
    public ResultEntity updateAvater(String userId, String base64){
        if (StringUtils.isEmpty(base64)) {
            return ResultUtil.fail("请选择文件");
        }
        String ext = base64.replaceAll(";base64,.+","").replaceAll("data:image/","");

        base64 = base64.replaceAll("data:image/.+base64,","");
        String imgName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String savePath = avaterPath+ imgName;
        String newImgName = Common.generateImage(base64, savePath);
        if(newImgName != null){
            userMapper.updateAvater(newImgName, userId);
            return ResultUtil.success(newImgName);
        }else{
            return ResultUtil.fail("修改头像失败");
        }
    }

    /**
     * @author: wuwenqiang
     * @methodsName: sendSimpleMail
     * @description: 发送文本邮件
     * @return: ResultEntity
     * @date: 2025-01-23 21:42
     */
    @Override
    public ResultEntity sendEmailVertifyCode(MailEntity mailRequest){
        if(!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", mailRequest.getEmail())){
            return ResultUtil.fail(null,"邮箱格式错误");
        }else if(userMapper.vertifyUserByEmail(mailRequest.getEmail()).size() == 0){
            return  ResultUtil.fail(null,"该账号不存在");
        }
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        redisTemplate.opsForValue().set(mailRequest.getEmail(), code,5, TimeUnit.MINUTES);
        System.out.println("验证码：" + code);

        return ResultUtil.success(1,"验证码已发送到邮箱，请五分钟内完成操作");
    }

    /**
     * @author: wuwenqiang
     * @methodsName: resetPassword
     * @description: 发送文本邮件
     * @return: ResultEntity
     * @date: 2025-01-23 21:42
     */
    @Override
    @Transactional
    public ResultEntity resetPassword(ResetPasswordEntity resetPasswordEntity){
        Integer code = (Integer)redisTemplate.opsForValue().get(resetPasswordEntity.getEmail());
        if(code == null || code != resetPasswordEntity.getCode()){
            return ResultUtil.fail(null,"验证码无效");
        }else{
            userMapper.resetPassword(resetPasswordEntity);
            UserEntity userEntity = new UserEntity();
            userEntity.setEmail(resetPasswordEntity.getEmail());
            userEntity.setUserAccount(resetPasswordEntity.getEmail());
            userEntity.setPassword(resetPasswordEntity.getPassword());
            UserEntity mUserEntity = userMapper.login(userEntity);
            String token = JwtToken.createToken(mUserEntity,secret);//token有效期30天
            redisTemplate.opsForValue().set(token, "1",30, TimeUnit.DAYS);
            ResultEntity resultEntity = ResultUtil.success(mUserEntity, "登录成功", token);
            resultEntity.setToken(token);
            return resultEntity;
        }
    }

    @Override
    public ResultEntity loginByEmail(MailEntity mailEntity){
        Integer code = (Integer) redisTemplate.opsForValue().get(mailEntity.getEmail());
        if(code == null || !mailEntity.getCode().equals(code.toString())){
            return ResultUtil.fail(null,"验证码无效");
        }else{
            UserEntity userEntity = userMapper.loginByEmail(mailEntity.getEmail());
            if (userEntity != null) {
                String token = JwtToken.createToken(userEntity,secret);//token有效期30天
                redisTemplate.opsForValue().set(token, "1",30, TimeUnit.DAYS);
                return ResultUtil.success(userEntity, "登录成功", token);
            } else {
                return ResultUtil.fail(null, "登录失败，邮箱不存在", ResultCode.FAIL);
            }
        }
    }

    @Override
    public ResultEntity searchUsers(String keyword, String tenantId) {
        // 如果关键词为空，返回空列表
        if (StringUtils.isEmpty(keyword)) {
            return ResultUtil.success(new ArrayList<>());
        }

        // 调用Mapper搜索用户
        List<SearchUserEntity> users = userMapper.searchUsers(keyword, tenantId);
        return ResultUtil.success(users);
    }
}