@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码提交脚本
echo ================================
echo.

REM 1. 启动SSH代理（如果已启动会跳过）
set SSH_AGENT_PID=
for /f "tokens=1-2 delims=: " %%a in ('ssh-agent -s 2^>nul') do (
    if "%%a"=="SSH_AUTH_SOCK" set SSH_AUTH_SOCK=%%b
    if "%%a"=="SSH_AGENT_PID" set SSH_AGENT_PID=%%b
)

REM 2. 添加SSH密钥
if "%SSH_AGENT_PID%"=="" (
    echo 正在启动SSH代理...
    ssh-agent -s > %TEMP%\ssh-agent.env
    call %TEMP%\ssh-agent.env
    del %TEMP%\ssh-agent.env
)

REM 尝试添加SSH密钥
ssh-add "%USERPROFILE%\.ssh\id_ed25519" 2>nul
if errorlevel 1 (
    echo 添加SSH密钥失败，尝试使用已有代理...
)

REM 3. 检查当前状态
echo.
echo 当前Git状态：
git status

REM 4. 添加所有更改
echo.
echo 添加所有更改文件...
git add .

REM 5. 提交更改
echo.
set /p commit_msg=请输入提交信息: 
if "%commit_msg%"=="" set commit_msg="自动提交 %date% %time%"
git commit -m "%commit_msg%"

REM 6. 推送到远程仓库
echo.
echo 正在推送到远程仓库...
git push origin master

REM 7. 显示结果
echo.
if errorlevel 1 (
    echo ❌ 推送失败！
    pause
    exit /b 1
) else (
    echo ✅ 推送成功！
    timeout /t 3 >nul
)