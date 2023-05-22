#!/bin/zsh

rm -Rf ~/projects/trade/src/main/resources/static/*
cd ~/projects/trade/front
npm run build
cd ~/projects/trade
mvn clean package -DskipTests

cp ~/projects/trade/target/trade-0.0.1-SNAPSHOT.jar ~/bin/trade.jar

sudo service trade stop
sudo service trade start
