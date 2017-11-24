@ECHO OFF
gradle clean build -Prelease -x Test %*
