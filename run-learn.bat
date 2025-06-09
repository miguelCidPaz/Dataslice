@echo off
echo Ejecutando DataSlice en modo LEARN sobre carpeta data\
java -Xms2G -Xmx2G -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -jar target/dataslice-0.0.1-SNAPSHOT.jar data
pause