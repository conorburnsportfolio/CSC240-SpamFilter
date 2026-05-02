@echo off
REM Build / test script for the CSC 240 Spam Filter project (Windows).
REM Mirrors build.sh.
REM
REM Usage:
REM   build.bat           : compile main + tests, run tests
REM   build.bat run [csv] : compile main, run on the given CSV
REM   build.bat compile   : compile main only
REM   build.bat test      : compile main + tests, run tests
REM   build.bat clean     : remove build artefacts

setlocal EnableDelayedExpansion
cd /d "%~dp0"

set SRC_MAIN=src\main\java
set SRC_TEST=src\test\java
set OUT_MAIN=out\main
set OUT_TEST=out\test
set LIB=lib

set CMD=%~1
if "%CMD%"=="" set CMD=test

if /I "%CMD%"=="clean" (
    if exist out     rmdir /s /q out
    if exist output  rmdir /s /q output
    echo cleaned.
    goto :eof
)

if /I "%CMD%"=="compile" goto :do_compile

if /I "%CMD%"=="run" (
    call :do_compile
    if errorlevel 1 exit /b 1
    if not exist output mkdir output
    set "INPUT=%~2"
    if "!INPUT!"=="" set "INPUT=data\spam_or_not_spam.csv"
    java -cp "%OUT_MAIN%" Main "!INPUT!" output
    goto :eof
)

if /I "%CMD%"=="test" (
    call :do_compile
    if errorlevel 1 exit /b 1
    if not exist "%OUT_TEST%" mkdir "%OUT_TEST%"
    set "JAR="
    for %%j in ("%LIB%\junit-platform-console-standalone-*.jar") do set "JAR=%%~fj"
    if "!JAR!"=="" (
        echo ERROR: no JUnit console jar found in %LIB%\
        echo   download junit-platform-console-standalone-1.10.2.jar into .\lib\
        exit /b 1
    )
    pushd "%SRC_TEST%"
    javac -cp "%~dp0%OUT_MAIN%;!JAR!" -d "%~dp0%OUT_TEST%" *.java
    set RC=!errorlevel!
    popd
    if "!RC!" NEQ "0" exit /b !RC!
    java -jar "!JAR!" --class-path "%OUT_MAIN%;%OUT_TEST%" --scan-class-path
    goto :eof
)

echo usage: %~nx0 [compile^|run [csv]^|test^|clean]
exit /b 2

:do_compile
if not exist "%OUT_MAIN%" mkdir "%OUT_MAIN%"
pushd "%SRC_MAIN%"
javac -d "%~dp0%OUT_MAIN%" *.java
set RC=%errorlevel%
popd
if "%RC%"=="0" (
    echo compiled main -^> %OUT_MAIN%
) else (
    echo compile failed.
)
exit /b %RC%
