@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码推送脚本（SSH方式）
echo 专用仓库：springboot3-app-service
echo ================================
echo.

REM 强制设置为SSH地址
set "SSH_REPO=git@gitee.com:wuyuanwuhui99/springboot3-app-service.git"
echo 设置远程仓库为SSH地址...
git remote remove origin 2>nul
git remote add origin "%SSH_REPO%"
echo 远程地址已设置为：%SSH_REPO%
echo.

REM 启动SSH代理并添加密钥
echo 初始化SSH连接...
ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
if errorlevel 1 (
    echo SSH密钥未加载，启动代理...
    ssh-agent -s > %TEMP%\ssh-agent.env
    call %TEMP%\ssh-agent.env
    del %TEMP%\ssh-agent.env
    ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
)

REM 测试SSH连接
echo.
echo 测试SSH连接...
ssh -T git@gitee.com 2>nul
if errorlevel 1 (
    echo ⚠ SSH连接测试失败
    echo 请确认：
    echo 1. SSH公钥已添加到Gitee
    echo 2. 公钥：%USERPROFILE%\.ssh\id_ed25519.pub
    echo 3. 在 https://gitee.com/profile/sshkeys 添加公钥
) else (
    echo ✓ SSH连接测试通过
)

REM 推送代码
echo.
echo 正在推送到远程仓库...
git push origin master

REM 显示结果
if errorlevel 1 (
    echo.
    echo ❌ 推送失败！
    echo 可能原因：
    echo 1. SSH密钥未正确配置
    echo 2. 没有提交内容（先运行 git commit）
    echo 3. 网络问题
    echo.
    echo 解决方案：
    echo 1. 检查SSH公钥是否添加到Gitee
    echo 2. 在Git Bash中运行：ssh -T git@gitee.com
    echo 3. 或切换到HTTPS：git remote set-url origin https://gitee.com/wuyuanwuhui99/springboot3-app-service.git
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    echo 仓库地址：%SSH_REPO%
    timeout /t 3 >nul
)