Remove-Item D:\projects\trade\src\main\resources\static\* -Recurse -Force

Set-Location D:\projects\trade\front

npm run build

Set-Location D:\projects\trade

mvn clean package -DskipTests

Copy-Item D:\projects\trade\target\trade-0.0.1-SNAPSHOT.jar -Destination D:\bin\trade.jar
