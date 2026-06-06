package com.player.company.mapper;

import com.player.company.entity.CompanyEntity;
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
    Integer selectUserRoleInCompany(@Param("userId") String userId,
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
     * @param companyUser 企业用户关联实体
     * @return 添加数量
     */
    Integer insertCompanyUser(CompanyUserEntity companyUserEntity);
}