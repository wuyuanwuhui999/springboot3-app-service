package com.player.company.service.imp;

import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.common.entity.UserEntity;
import com.player.common.utils.ResultCode;
import com.player.company.entity.CompanyDepartmentEntity;
import com.player.company.entity.CompanyEntity;
import com.player.company.entity.CompanyPositionEntity;
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

    private static final String BOARD_DIRECTORS_DEPARTMENT_ID = "f6a7b8c9d0e142f3a4b5c6d7e8f9a0b1";

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
        CompanyUserEntity currentUser = companyMapper.getCompanyUserByUserId(userId, companyId);
        if (currentUser == null || currentUser.getRole() <= 0) {
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

        // 默认角色为0，不能为负数
        Integer targetRole = companyUser.getRole();
        if (targetRole == null) {
            targetRole = 0;
        }
        // 角色范围校验（必须大于等于0）
        if (targetRole < 0 || targetRole > 1) {
            return ResultUtil.fail(null, "角色值无效，有效范围：0-1", ResultCode.FAIL);
        }

        // 1. 查询当前登录人在企业中的关联信息
        CompanyUserEntity currentUser = companyMapper.getCompanyUserByUserId(userId, companyUser.getCompanyId());
        if (currentUser == null) {
            return ResultUtil.fail(null, "当前用户不是该企业的成员，无权添加成员", ResultCode.FAIL);
        }

        // 2. 权限校验：角色必须大于0才能添加成员，且不能添加角色等级大于或等于自己的用户
        if (currentUser.getRole() <= 0) {
            return ResultUtil.fail(null, "普通成员无权添加成员", ResultCode.FAIL);
        }
        if (targetRole >= currentUser.getRole()) {
            return ResultUtil.fail(null, "无权添加角色等级高于或等于自己的用户", ResultCode.FAIL);
        }

        // 3. 检查用户是否已在企业中
        CompanyUserEntity existingUser = companyMapper.getCompanyUserByUserId(companyUser.getUserId(), companyUser.getCompanyId());
        if (existingUser != null) {
            return ResultUtil.fail(null, "该用户已存在于企业中", ResultCode.FAIL);
        }

        // 4. 检查要添加的职位是否属于董事会部门
        //    如果目标职位属于董事会部门，需要校验当前用户角色是否大于1（即管理员及以上才能添加董事会用户）
        if (companyUser.getPositionId() != null && !companyUser.getPositionId().isEmpty()) {
            // 查询该职位所在的部门
            CompanyPositionEntity position = companyMapper.getPositions(companyUser.getPositionId())
                    .stream()
                    .filter(p -> p.getId().equals(companyUser.getPositionId()))
                    .findFirst()
                    .orElse(null);

            if (position != null) {
                String departmentId = position.getDepartmentId();
                // 如果是董事会部门
                if (BOARD_DIRECTORS_DEPARTMENT_ID.equals(departmentId)) {
                    // 检查当前用户角色是否大于1（管理员或超级管理员）
                    if (currentUser.getRole() == null || currentUser.getRole() <= 1) {
                        return ResultUtil.fail(null, "无权限添加董事会部门的用户，需要管理员或超级管理员权限");
                    }
                }
            }
        }

        // 5. 构建关联数据并插入
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

    /**
     * 查询公司用户列表（支持关键字模糊搜索）
     */
    @Override
    public ResultEntity searchCompanyUsers(String userId, String companyId, Integer pageNum, Integer pageSize, String keyword) {
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

        // 校验当前用户是否在该企业中（只有企业成员才能查看企业用户列表）
        CompanyUserEntity currentUser = companyMapper.getCompanyUserByUserId(userId, companyId);
        if (currentUser == null) {
            return ResultUtil.fail(null, "您不是该企业的成员，无权查看", ResultCode.FAIL);
        }

        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        List<UserEntity> users = companyMapper.searchCompanyUserByKeyword(companyId, keyword, offset, pageSize);
        Long total = companyMapper.countCompanyUserByKeyword(companyId, keyword);

        return ResultUtil.success(users, total);
    }

    /**
     * 根据公司ID查询所有部门
     */
    @Override
    public ResultEntity getDepartments(String userId, String companyId) {
        // 参数校验
        if (StringUtils.isEmpty(companyId)) {
            return ResultUtil.fail(null, "企业ID不能为空", ResultCode.FAIL);
        }

        // 权限校验：检查当前用户是否是该企业的成员
        CompanyUserEntity currentUser = companyMapper.getCompanyUserByUserId(userId, companyId);
        if (currentUser == null) {
            return ResultUtil.fail(null, "您不是该企业的成员，无权查看部门信息", ResultCode.FAIL);
        }

        // 查询部门列表
        List<CompanyDepartmentEntity> departments = companyMapper.getDepartments(companyId);
        return ResultUtil.success(departments);
    }

    /**
     * 根据部门ID查询所有职位
     */
    @Override
    public ResultEntity getPositions(String userId, String departmentId) {
        // 参数校验
        if (StringUtils.isEmpty(departmentId)) {
            return ResultUtil.fail(null, "部门ID不能为空", ResultCode.FAIL);
        }

        // 查询职位列表
        List<CompanyPositionEntity> positions = companyMapper.getPositions(departmentId);
        return ResultUtil.success(positions);
    }
}