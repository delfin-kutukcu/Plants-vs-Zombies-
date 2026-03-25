@echo off
echo Derleniyor...
if not exist out mkdir out
javac -encoding UTF-8 -d out -sourcepath src src\pvz\Main.java
if %ERRORLEVEL% neq 0 (
    echo HATA: Derleme basarisiz!
    pause
    exit /b 1
)
echo Derleme basarili! Oyun baslatiliyor...
java -cp "out;resources" pvz.Main
