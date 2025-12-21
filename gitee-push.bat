@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码推送脚本
echo 专用仓库：https://gitee.com/wuyuanwuhui99/springboot3-app-service.git
echo ================================
echo.

REM 强制设置仓库地址为HTTPS地址
set "TARGET_REPO=https://gitee.com/wuyuanwuhui99/springboot3-app-service.git"
echo 设置远程仓库地址...
git remote remove origin 2>nul
git remote add origin "%TARGET_REPO%"

REM 显示设置的地址
echo 当前远程地址：
git remote get-url origin
echo.

REM 如果你还需要添加SSH密钥（虽然HTTPS地址不需要SSH）
REM 但如果你其他仓库需要，可以保留这部分
echo 初始化SSH连接（可选）...
ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
if errorlevel 1 (
    echo SSH密钥未加载，跳过SSH初始化...
)

REM 推送代码
echo.
echo 正在推送到远程仓库...
git push origin master

REM 显示结果
if errorlevel 1 (
    echo.
    echo ❌ 推送失败！
    echo 此仓库使用HTTPS地址，需要：
    echo 1. Gitee用户名密码
    echo 2. 或私人令牌（如果启用了双重认证）
    echo.
    echo 请手动运行：git push origin master
    echo 然后输入凭据
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    timeout /t 2 >nul
)