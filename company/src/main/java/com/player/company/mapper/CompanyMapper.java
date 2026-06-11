package com.player.company.mapper;

import com.player.company.entity.CompanyDepartmentEntity;
import com.player.company.entity.CompanyEntity;
import com.player.company.entity.CompanyPositionEntity;
import com.player.company.entity.CompanyUserEntity;
import com.player.common.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyMapper {

    /**
     * 根据用户ID查询所属公司列表
     * @param userId 用户ID
     * @return 公司列表（包含角色和默认企业标识）
     */
    List<CompanyEntity> selectCompanyListByUserId(@Param("userId") String userId);

    /**
     * 查询公司成员列表（分页）
     * @param companyId 企业ID
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<UserEntity> selectCompanyUsers(@Param("companyId") String companyId,
                                        @Param("offset") Integer offset,
                                        @Param("pageSize") Integer pageSize);

    /**
     * 统计公司成员总数
     * @param companyId 企业ID
     * @return 成员总数
     */
    Long countCompanyUsers(@Param("companyId") String companyId);

    /**
     * 查询用户在企业中的角色
     * @param userId 用户ID
     * @param companyId 企业ID
     * @return 角色值，未找到返回null
     */
    Integer getUserRole(@Param("userId") String userId,
                                    @Param("companyId") String companyId);

    /**
     * 检查用户是否已在企业中
     * @param userId 用户ID
     * @param companyId 企业ID
     * @return 存在返回数量
     */
    Integer checkUserExistsInCompany(@Param("userId") String userId,
                                     @Param("companyId") String companyId);

    /**
     * 添加用户到企业
     * @param companyUserEntity 企业用户关联实体
     * @return 添加数量
     */
    Integer insertCompanyUser(CompanyUserEntity companyUserEntity);

    /**
     * 查询公司用户列表（支持关键字模糊搜索）
     * @param companyId 企业ID
     * @param keyword 搜索关键字
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<UserEntity> searchCompanyUserByKeyword(@Param("companyId") String companyId,
                                                @Param("keyword") String keyword,
                                                @Param("offset") Integer offset,
                                                @Param("pageSize") Integer pageSize);

    /**
     * 统计公司用户数量（支持关键字模糊搜索）
     * @param companyId 企业ID
     * @param keyword 搜索关键字
     * @return 用户总数
     */
    Long countCompanyUserByKeyword(@Param("companyId") String companyId,
                                   @Param("keyword") String keyword);

    /**
     * 根据公司ID查询所有部门
     * @param companyId 企业ID
     * @return 部门列表
     */
    List<CompanyDepartmentEntity> getDepartments(@Param("companyId") String companyId);

    /**
     * 根据部门ID查询所有职位
     * @param departmentId 部门ID
     * @return 职位列表
     */
    List<CompanyPositionEntity> getPositions(@Param("departmentId") String departmentId);
}