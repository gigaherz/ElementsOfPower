@echo off

set PATH=%PATH%;"C:\Program Files\Java\jdk1.7.0_71\bin"

gradlew genIntellijRuns

if errorlevel 1 do pause