@echo off
:start
echo Choose what version to build: 
echo 1) Fabric-1.14
echo 2) Fabric-1.15
echo 3) Fabric-1.16
echo.

set choice=
set /p choice=Choice: 
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto 1_14
if '%choice%'=='2' goto 1_15
if '%choice%'=='3' goto 1_16
echo "%choice%" is not valid, try again
echo.
goto start

:1_14
echo.
echo Building BleachHack-Fabric-1.14..
cd BleachHack-Fabric-1.14/
gradlew build
goto end

:1_15
echo.
echo Building BleachHack-Fabric-1.15..
cd BleachHack-Fabric-1.15/
gradlew build
goto end

:1_16
echo.
echo Building BleachHack-Fabric-1.16..
cd BleachHack-Fabric-1.16/
gradlew build
goto end
pause