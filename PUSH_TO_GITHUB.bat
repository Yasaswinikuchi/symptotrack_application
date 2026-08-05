@echo off
title Push SymptoTrack to GitHub
echo ====================================================
echo Pushing SymptoTrack Pro to GitHub Repository...
echo ====================================================
echo.
set "GIT_PATH=C:\Users\yasaswini kuchi\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd\git.exe"
cd /d "c:\Users\yasaswini kuchi\Downloads\SymtoTrack_Source"

if exist "%GIT_PATH%" (
    "%GIT_PATH%" push -u origin main --force
) else (
    git push -u origin main --force
)

echo.
echo ====================================================
echo SUCCESS! All files, workflows, and test reports uploaded!
echo Refresh your browser: https://github.com/Yasaswinikuchi/symptotrack_app
echo ====================================================
pause
