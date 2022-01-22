#!/bin/sh
curl -k --trace - -o /dev/null https://localhost:8443/api 2>/dev/null | tee trace.log
java -jar target/playground-http-0.0.1-SNAPSHOT.jar client 2>&1 | tee fail.log
java -jar target/playground-http-0.0.1-SNAPSHOT.jar fix 2>&1 | tee ok.log
