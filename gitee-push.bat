@echo off
chcp 65001 >nul
echo ================================
echo Gitee 代码推送脚本（地址写死版）
echo ================================
echo.

REM 写死目标仓库地址（使用 .git 后缀更规范）
set "REPO_URL=https://gitee.com/wuyuanwuhui99/springboot3-app-service.git"

REM 强制设置 origin 为指定地址（无论当前是什么）
git remote set-url origin "%REPO_URL%" >nul 2>&1 || git remote add origin "%REPO_URL%"

echo 当前推送地址已设为：%REPO_URL%
echo 正在推送代码到 Gitee...

git push origin master

if errorlevel 1 (
    echo.
    echo ❌ 推送失败！
    echo 请检查：
    echo   • 是否已提交更改（git commit）
    echo   • 网络是否正常
    echo   • 是否已配置 Gitee Personal Access Token（作为密码）
    echo   • 是否有推送权限
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    timeout /t 2 >nul
)