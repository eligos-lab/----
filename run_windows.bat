@echo off
echo 📧 Building and running Temp Email Service...
mvn clean package exec:java -Dexec.mainClass=MainFrame
pause