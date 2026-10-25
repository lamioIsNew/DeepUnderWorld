@echo off
setlocal
cd /d "%~dp0"

echo [1/5] Generating block/item assets and texture pack...
python modpack\tools\generate_assets.py
if errorlevel 1 goto fail

echo [2/5] Building the WCNM mod jar...
call gradlew.bat build --console=plain
if errorlevel 1 goto fail

echo [3/5] Building the DeepUnderWorld modpack...
python modpack\tools\build_pack.py
if errorlevel 1 goto fail

echo [4/5] Deploying to D:\PCL\.minecraft...
python modpack\tools\deploy_to_pcl.py
if errorlevel 1 goto fail

echo [5/5] Installing the launchable PCL2 version...
python modpack\tools\install_pcl_version.py
if errorlevel 1 goto fail

echo.
echo Done. Launch "Deep Under World 2.4.1" from PCL2.
exit /b 0

:fail
echo.
echo Build or deployment failed.
exit /b 1
