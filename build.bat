@echo off
cd /d D:\Projects\project\Blog\Blog
call D:\Softwave\Code\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
pause
