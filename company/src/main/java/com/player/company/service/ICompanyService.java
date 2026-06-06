package com.player.company.service;

import com.player.common.entity.ResultEntity;
import com.player.company.entity.CompanyUserEntity;

public interface ICompanyService {

    /**
     * 获取当前用户所属的公司列表
     * @param userId 当前登录用户ID（从请求头获取）
     * @return 公司列表
     */
    ResultEntity getCompanyList(String userId);

    /**
     * 获取公司成员列表（分页）
     * @param userId 当前登录用户ID
     * @param companyId 企业ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 成员列表（分页）
     */
    ResultEntity getCompanyUsers(String userId, String companyId, Integer pageNum, Integer pageSize);

    /**
     * 添加用户到公司
     * @param userId 当前登录用户ID
     * @param companyUser 企业用户关联信息（包含companyId, userId, role）
     * @return 添加结果
     */
    ResultEntity addUser(String userId, CompanyUserEntity companyUser);

    /**
     * 查询公司用户列表（支持关键字模糊搜索）
     * @param userId 当前登录用户ID
     * @param companyId 企业ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键字（模糊匹配 username/user_account/telephone/email）
     * @return 公司用户列表（分页）
     */
    ResultEntity getCompanyUser(String userId, String companyId, Integer pageNum, Integer pageSize, String keyword);
}