@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码推送脚本
echo ================================
echo.

REM 检查远程地址
echo 检查远程仓库地址...
git remote get-url origin >nul 2>nul
if errorlevel 1 (
    echo 未设置远程仓库，正在设置...
    git remote add origin git@gitee.com:wuyuanwuhui99/springboot3-app-service.git
) else (
    echo 当前远程地址：
    git remote get-url origin
)

REM 启动SSH代理并添加密钥
echo.
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
    echo 如果使用的是HTTPS地址，请运行以下命令切换到SSH：
    echo git remote set-url origin git@gitee.com:wuyuanwuhui99/springboot3-app-service.git
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    timeout /t 2 >nul
)