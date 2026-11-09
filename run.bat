@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -d build Main.java
if errorlevel 1 goto end
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp build Main
:end
pause
