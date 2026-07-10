@echo off
set MUTED_FILE=%~dp0.muted
if /I "%1"=="status" goto status
if /I "%1"=="on" goto mute
if /I "%1"=="off" goto unmute
if /I "%1"=="" goto toggle
goto :eof

:toggle
if exist "%MUTED_FILE%" (del "%MUTED_FILE%" & echo 通知声音已开启) else (echo. > "%MUTED_FILE%" & echo 通知声音已关闭)
exit /b

:mute
echo. > "%MUTED_FILE%" & echo 通知声音已关闭
exit /b

:unmute
if exist "%MUTED_FILE%" del "%MUTED_FILE%"
echo 通知声音已开启
exit /b

:status
if exist "%MUTED_FILE%" (echo 当前: 静音) else (echo 当前: 声音开启)
exit /b