@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码推送脚本
echo ================================
echo.

REM 启动SSH代理并添加密钥
echo 初始化SSH连接...
ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
if errorlevel 1 (
    echo SSH密钥未加载，尝试启动代理...
    ssh-agent -s > %TEMP%\ssh-agent.env
    call %TEMP%\ssh-agent.env
    del %TEMP%\ssh-agent.env
    ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
)

REM 推送代码
echo.
echo 正在推送到远程仓库...
git push origin master

REM 显示结果
if errorlevel 1 (
    echo.
    echo ❌ 推送失败！
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    timeout /t 2 >nul
)