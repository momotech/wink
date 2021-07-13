#!/bin/bash

sed -i '' 's/gradle.ext.uploadMavenCenter=true/gradle.ext.uploadMavenCenter=false/g' UploadProperties.gradle
./gradlew -p buildSrc :wink-gradle-plugin:uploadArchives

# package to CDN
cp ./wink-gradle-plugin/build/libs/wink-gradle-plugin.jar ./.idea/wink/lib
sh .idea/wink/lib/wink.sh