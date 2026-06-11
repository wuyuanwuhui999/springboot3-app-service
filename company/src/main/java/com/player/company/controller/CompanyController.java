package com.player.company.controller;

import com.player.common.entity.ResultEntity;
import com.player.company.entity.CompanyUserEntity;
import com.player.company.service.ICompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service/company")
@Tag(name = "企业模块", description = "企业相关接口")
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    /**
     * 获取当前用户所属的公司列表
     */
    @GetMapping("/getCompanyList")
    @Operation(summary = "获取用户所属公司列表")
    public ResultEntity getCompanyList(@RequestHeader("X-User-Id") String userId) {
        return companyService.getCompanyList(userId);
    }

    /**
     * 获取公司成员列表（分页）
     */
    @GetMapping("/getCompanyUsers")
    @Operation(summary = "获取公司成员列表")
    public ResultEntity getCompanyUsers(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("companyId") String companyId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return companyService.getCompanyUsers(userId, companyId, pageNum, pageSize);
    }

    /**
     * 添加用户到公司
     */
    @PostMapping("/addUser")
    @Operation(summary = "添加用户到公司")
    public ResultEntity addUser(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CompanyUserEntity companyUser) {
        return companyService.addUser(userId, companyUser);
    }

    /**
     * 查询公司用户列表（支持关键字模糊搜索）
     */
    @GetMapping("/searchCompanyUsers")
    @Operation(summary = "查询公司用户列表")
    public ResultEntity getCompanyUser(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("companyId") String companyId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return companyService.searchCompanyUsers(userId, companyId, pageNum, pageSize, keyword);
    }

    /**
     * 根据公司ID查询所有部门
     */
    @GetMapping("/getDepartments")
    @Operation(summary = "根据公司ID查询所有部门")
    public ResultEntity getDepartments(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("companyId") String companyId) {
        return companyService.getDepartments(userId, companyId);
    }

    /**
     * 根据部门ID查询所有职位
     */
    @GetMapping("/getPositions")
    @Operation(summary = "根据部门ID查询所有职位")
    public ResultEntity getPositions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("departmentId") String departmentId) {
        return companyService.getPositions(userId, departmentId);
    }
}