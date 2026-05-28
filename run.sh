#!/bin/bash

# Navigate to the project directory
cd "$(dirname "$0")"

echo "Compiling MusicTick JavaFX application..."
mkdir -p out

# Compile Java files
javac --module-path libs/javafx-lib --add-modules javafx.controls,javafx.fxml -d out -cp libs/mysql-connector-java.jar $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "Copying FXML resource files..."
    cp src/*.fxml out/
    
    echo "Running MusicTick..."
    java --module-path libs/javafx-lib \
         --add-modules javafx.controls,javafx.fxml \
         --enable-native-access=javafx.graphics \
         --sun-misc-unsafe-memory-access=allow \
         -cp out:libs/mysql-connector-java.jar \
         com.musictick.Main
else
    echo "Compilation failed."
    exit 1
fi
