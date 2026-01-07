@echo off
setlocal enabledelayedexpansion

:: 设置远程仓库
git remote rm origin
git remote add origin https://github.com/wuyuanwuhui999/springboot3-app-service.git

:: 推送设置
set max_retries=5
set retry_count=0
set push_success=0

echo 开始推送代码到GitHub...

:push_retry
set /a retry_count+=1
echo 第!retry_count!次尝试推送...

:: 尝试推送，设置超时为10秒
git push origin master --progress
if !errorlevel! equ 0 (
    set push_success=1
    echo 推送成功！
    goto :push_complete
) else (
    echo 推送失败...
)

:: 检查是否达到最大重试次数
if !retry_count! lss !max_retries! (
    echo 等待10秒后重试...
    timeout /t 10 /nobreak > nul
    goto :push_retry
) else (
    echo 已达最大重试次数(!max_retries!次)，推送失败
)

:push_complete
if !push_success! equ 1 (
    echo 推送成功，窗口将在3秒后自动关闭...
    timeout /t 3 /nobreak > nul
    exit
) else (
    echo 请检查网络连接后重试
    pause
    exit /b 1
)