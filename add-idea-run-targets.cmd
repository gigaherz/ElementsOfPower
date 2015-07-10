@echo off

set PATH=%PATH%;"C:\Program Files\Java\jdk1.8.0_45\bin"

gradlew genIntellijRuns

if errorlevel 1 do pause