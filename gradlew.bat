@rem Gradle wrapper script for Windows
@rem Neon City RPG

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem  Gradle startup script for Windows
@rem ##########################################################################

@rem Set local scope for the variables
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Gradle version
set GRADLE_VERSION=8.2
set GRADLE_DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip

@rem Find GRADLE_USER_HOME
if defined GRADLE_USER_HOME goto setGradleHome
set GRADLE_USER_HOME=%USERPROFILE%\.gradle

:setGradleHome
set GRADLE_DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin
set GRADLE_HOME=%GRADLE_DIST_DIR%\gradle-%GRADLE_VERSION%

@rem Check if Gradle is already downloaded
if exist "%GRADLE_HOME%\bin\gradle.bat" goto execute

@rem Download Gradle
echo Downloading Gradle %GRADLE_VERSION%...
if not exist "%GRADLE_DIST_DIR%" mkdir "%GRADLE_DIST_DIR%"

@rem Try PowerShell download
powershell -Command "& {Invoke-WebRequest -Uri '%GRADLE_DIST_URL%' -OutFile '%GRADLE_DIST_DIR%\gradle.zip'}"
if errorlevel 1 (
    echo Failed to download Gradle. Please check your internet connection.
    exit /b 1
)

@rem Extract
echo Extracting Gradle...
powershell -Command "& {Expand-Archive -Path '%GRADLE_DIST_DIR%\gradle.zip' -DestinationPath '%GRADLE_DIST_DIR%' -Force}"
del "%GRADLE_DIST_DIR%\gradle.zip"
echo Gradle %GRADLE_VERSION% installed.

:execute
@rem Setup Java
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %errorlevel% equ 0 goto runGradle

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto runGradle

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
exit /b 1

:runGradle
"%GRADLE_HOME%\bin\gradle.bat" %*

:end
endlocal
