#!/usr/bin/env sh
set -e

export JAVA_OPTIONS="$JAVA_OPTIONS -Xmx5120M -XX:MaxPermSize=2048M "

export CP=`find "../lib" -name '*.jar' | xargs echo | tr ' ' ':'`
export JAR=`find "../" -name 'spray-io-file-service.jar' | xargs echo | tr ' ' ':'`

java -classpath $CP:$JAR $JAVA_OPTIONS lecko.service.FileManagerServiceApp $@