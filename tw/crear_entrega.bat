@echo off
chcp 65001 > nul
echo.
echo  =============================================
echo   CUMe y Calla - Generar ZIP de entrega
echo  =============================================
echo.

set "BASE=%~dp0"
set "ZIP=%BASE%CUMe_y_Calla_EntregaAct3.zip"

:: Borrar ZIP anterior si existe
if exist "%ZIP%" del /f /q "%ZIP%"

echo  Generando ZIP...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
 "Add-Type -Assembly System.IO.Compression.FileSystem;" ^
 "$src  = '%BASE%'.TrimEnd('\');" ^
 "$zip  = '%ZIP%';" ^
 "$items = @('src','pom.xml','TANI.db','CUMe_y_Calla_API.postman_collection.json');" ^
 "foreach ($f in @('evidencias_api.pdf','evidencias_api.html','credenciales.txt')) { if (Test-Path \"$src\$f\") { $items += $f } };" ^
 "Compress-Archive -Path ($items | ForEach-Object { \"$src\$_\" }) -DestinationPath $zip -Force;"

if exist "%ZIP%" (
    echo.
    echo  ZIP creado correctamente:
    echo  %ZIP%
    echo.
    echo  Recuerda entregar antes del 04/05/2026 a las 23:59h
) else (
    echo.
    echo  ERROR: No se pudo crear el ZIP.
    echo  Prueba a ejecutar el script como Administrador.
)

echo.
pause
