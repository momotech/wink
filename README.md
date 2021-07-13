English | [简体中文](./README-zh.md)

## Wink

Wink is a quick build plugin for AndroidStudio. Wink's goal is to build an easy-to-use and easy-to-maintain Android quick build plugin. The difference between us and other quick build plugins is that our quick build process is fully implemented using Gradle and Java language, supporting quick compilation of java files, kotlin files and resource changes.


## Wink Insight 
During Android development, the build speed of the application directly affects the productivity of the developer's day. For large Android projects that have been maintained for several years, a single line of code change can potentially take more than 10 minutes to compile. Although Instant Run can bring some optimization, the optimization result is still minute level, Wink is created to solve this problem, for simple code changes, the compilation speed is improved to second level. Based on Gradle and Groovy, we optimize the compilation process for java, kotlin and resource files, and give the whole process to Gradle's Task system without using other scripting languages, which not only ensures the readability and ease of use of the project code, but also greatly improves the maintainability of the code.

## Features 
1. Java change support
2. kotlin change support
3. Gradle mutil module support
4. Res/values  change support


## Install 
### Install by AndroidStuido Plugin
1. Download the Plugin from the [Wink.zip](https://s.momocdn.com/s1/u/dcehhhadi/Wink-0.0.2.zip)
2. Open AndroidStudio 
3. Drag Wink.zip to AndroidStudio 
4. Restart AndroidStudio

## Run by AndroidStudio Plugin
1. When you install plugin success. After you open a project  you can see wink button like this screenshot. 

    ![wink_btn](https://s.momocdn.com/s1/u/dcehhhadi/wink_btn.png)


2. Click wink button you can see dialog like this.Choose your project‘s root build.gradle file and main app build.gradle file. Automic change gradle file by click install btn.
   
    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_config_dialog.jpeg" width = "500"  alt="图片名称" align=center />


3. Click wink button again ,you will see log on wink console.
   
    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_console.png" width = "500"  alt="图片名称" align=center />



### Install by Gradle
1 . Find your build.gradle in your project root folder, and add line classpath 'com.immomo.litebuild:plugin:0.1.1' in dependencies block
```
   buildscript {
    repositories {
         mavenCentral()//use mavenCenter
    }
    dependencies {
        classpath 'com.immomo.litebuild:plugin:0.1.45'
    }
}
```
2 . In your main app folder find build.gradle file . Insert line plugin config like demo code
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


## Run by Gradle or Terminal
1. After install plugin you can run by Wink button or run by gradle task litebuild in gradle task list
   
    <img src="https://s.momocdn.com/s1/u/dcehhhadi/gradle_task_lite_build.png" width = "330" height = "305" alt="图片名称" align=center />


## The MIT License (MIT)
```
Copyright (c) 2021 NASDAQ：MOMO
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
    