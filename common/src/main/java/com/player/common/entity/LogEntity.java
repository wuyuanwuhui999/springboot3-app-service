package com.player.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;

public class LogEntity implements Serializable {

    @Schema(description = "主键")
    private int id;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求url")
    private String url;

    @Schema(description = "请求头")
    private String headers;

    @Schema(description = "ip地址")
    private String ip;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "url上面的参数")
    private String queryString;

    @Schema(description = "请求结果")
    private String result;

    @Schema(description = "请求开始时间")
    private Date startTime;

    @Schema(description = "耗时")
    private Long runTime;

    @Schema(description = "接口描述")
    private String description;

    @Schema(description = "请求结束时间")
    private Date endTime;

    @Schema(description = "操作类型")
    private String oparation;

    @Schema(description = "请求类型，get,post,put")
    private String type;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "应用id")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getOparation() {
        return oparation;
    }

    public void setOparation(String oparation) {
        this.oparation = oparation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
