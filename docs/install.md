# Install Wink

## Install by AndroidStuido Plugin
1. Download the Plugin from the [Wink.zip](https://s.momocdn.com/s1/u/geajgghjh/Wink-0.0.1.zip)
2. Open AndroidStudio 
3. Drag Wink.zip to AndroidStudio 
4. Restart AndroidStudio


## Install by Gradle
1. Find your build.gradle in your project root folder, and add line classpath 'com.immomo.litebuild:plugin:0.1.1' in dependencies block
```
buildscript {
    repositories {
        mavenCentral()//use mavenCenter
    }
    dependencies {
        classpath 'com.immomo.litebuild:plugin:0.1.51'
    }
}
```
2. In your main app folder find build.gradle file . Insert line plugin config like demo code
```
plugins {
    id 'com.android.application'
    id 'com.immomo.litebuild.plugin'
    id 'kotlin-android'
}
```
or like this 

    apply plugin: 'com.android.application'
    apply plugin: 'com.immomo.litebuild.plugin'

3. Sync gradle and you will sell lite-build task in your gradle Task list.