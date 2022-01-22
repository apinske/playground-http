#!/bin/sh
mvn clean package -DskipTests && java -jar target/playground-http-0.0.1-SNAPSHOT.jar
