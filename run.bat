@echo off
echo Compiling MusicTick JavaFX application on Windows...
if not exist out mkdir out

dir /s /b src\*.java > sources.txt
javac --module-path libs\javafx-lib --add-modules javafx.controls,javafx.fxml -d out -cp libs\mysql-connector-java.jar @sources.txt
del sources.txt

if %errorlevel% equ 0 (
    echo Copying FXML resource files...
    copy src\*.fxml out\
    
    echo Running MusicTick...
    java --module-path libs\javafx-lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics --sun-misc-unsafe-memory-access=allow -cp out;libs\mysql-connector-java.jar com.musictick.Main
) else (
    echo Compilation failed.
    pause
)
