package com.player.company.service.impl;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.entity.UserEntity;
import com.player.common.utils.ResultCode;
import com.player.company.entity.CompanyEntity;
import com.player.company.entity.CompanyUserEntity;
import com.player.company.mapper.CompanyMapper;
import com.player.company.service.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyService implements ICompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    /**
     * 获取当前用户所属的公司列表
     */
    @Override
    public ResultEntity getCompanyList(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return ResultUtil.fail(null, "用户ID不能为空", ResultCode.FAIL);
        }

        List<CompanyEntity> companyList = companyMapper.selectCompanyListByUserId(userId);
        return ResultUtil.success(companyList);
    }

    /**
     * 获取公司成员列表（分页）
     */
    @Override
    public ResultEntity getCompanyUsers(String userId, String companyId, Integer pageNum, Integer pageSize) {
        // 参数校验
        if (StringUtils.isEmpty(companyId)) {
            return ResultUtil.fail(null, "企业ID不能为空", ResultCode.FAIL);
        }

        // 分页参数默认值
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        // 校验当前用户是否有权限查看成员列表（role > 0）
        Integer currentUserRole = companyMapper.selectUserRoleInCompany(userId, companyId);
        if (currentUserRole == null || currentUserRole <= 0) {
            return ResultUtil.fail(null, "无权限查看该企业成员列表", ResultCode.FAIL);
        }

        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        List<UserEntity> users = companyMapper.selectCompanyUsers(companyId, offset, pageSize);
        Long total = companyMapper.countCompanyUsers(companyId);

        return ResultUtil.success(users, total);
    }

    /**
     * 添加用户到公司（包含角色权限校验）
     */
    @Override
    @Transactional
    public ResultEntity addUser(String userId, CompanyUserEntity companyUser) {
        // 参数校验
        if (StringUtils.isEmpty(companyUser.getCompanyId())) {
            return ResultUtil.fail(null, "企业ID不能为空", ResultCode.FAIL);
        }
        if (StringUtils.isEmpty(companyUser.getUserId())) {
            return ResultUtil.fail(null, "用户ID不能为空", ResultCode.FAIL);
        }

        // 默认角色为0
        Integer targetRole = companyUser.getRole();
        if (targetRole == null) {
            targetRole = 0;
        }
        // 角色范围校验（0-3）
        if (targetRole < 0 || targetRole > 3) {
            return ResultUtil.fail(null, "角色值无效，有效范围：0-3", ResultCode.FAIL);
        }

        // 1. 查询当前登录人在企业中的角色
        Integer currentUserRole = companyMapper.selectUserRoleInCompany(userId, companyUser.getCompanyId());
        if (currentUserRole == null || currentUserRole <= 0) {
            return ResultUtil.fail(null, "当前用户无权添加成员", ResultCode.FAIL);
        }

        // 2. 权限校验：不能添加角色 >= 自己的用户
        //    角色等级：0(普通) < 1(管理员) < 2(人事) < 3(老板)
        if (targetRole >= currentUserRole) {
            return ResultUtil.fail(null, "无权添加角色等级高于或等于自己的用户", ResultCode.FAIL);
        }

        // 3. 检查用户是否已在企业中
        Integer exists = companyMapper.checkUserExistsInCompany(companyUser.getUserId(), companyUser.getCompanyId());
        if (exists != null && exists > 0) {
            return ResultUtil.fail(null, "该用户已存在于企业中", ResultCode.FAIL);
        }

        // 4. 构建关联数据并插入
        CompanyUserEntity newRelation = new CompanyUserEntity();
        newRelation.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        newRelation.setUserId(companyUser.getUserId());
        newRelation.setCompanyId(companyUser.getCompanyId());
        newRelation.setRole(targetRole);
        newRelation.setStatus(1); // 默认正常状态
        newRelation.setJoinDate(LocalDateTime.now());
        newRelation.setCreateBy(userId); // 创建人为当前登录用户

        Integer result = companyMapper.insertCompanyUser(newRelation);
        return ResultUtil.success(result, "添加成功");
    }
}