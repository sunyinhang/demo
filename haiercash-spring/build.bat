@ECHO OFF
gradle clean build --refresh-dependencies -Prelease %*
