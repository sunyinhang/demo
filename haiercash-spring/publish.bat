@ECHO OFF
gradle clean build publish --refresh-dependencies -Prelease %*
