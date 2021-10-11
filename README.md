English | [简体中文](./README-zh.md)

## Wink

Wink is a quick build plugin for AndroidStudio.The goal of Wink is to build a quick build plugin for Android that is easy to use and easy to maintain. We differ from other quick build plugins in that our quick build process is implemented entirely in Gradle and Java language, supporting quick compilation of java files, kotlin files and resource changes.


## Wink Insight 
In Android development process, the build speed of the application directly affects the developer's productivity in a day. For large Android projects that have been maintained for several years, a single line of code change can potentially take more than 10 minutes to compile. While Instant Run can bring some optimization, the result of optimization is still minute, and Wink was created to solve this problem by increasing the compilation speed to seconds for simple code changes. Based on Gradle and Groovy, we optimize the compilation process of java, kotlin and resource files, and give the whole process to Gradle's Task system without using other scripting languages, which not only ensures the readability and ease of use of the project code, but also greatly improves the maintainability of the code.

## Features 
1. support for Java changes
2. support for kotlin changes
3. Gradle mutil module support
4. support for resource changes


## Installation 
### Install via AndroidStuido plugin
1. Download the plugin from [wink_studio_plugin.zip](https://s.momocdn.com/s1/u/geajgghjh/Wink-2.1.2.zip)
2. Open AndroidStudio 
3. Drag Wink.zip to AndroidStudio 
4. Restart AndroidStudio

## Run by AndroidStudio plugin
1. When you install the plugin successfully. After you open a project, you can see the wink button, like this screenshot.


2. Click the wink button, you can see this dialog box. Select the build.gradle file in the project root directory and the main application build.gradle file. The gradle file is automatically changed by clicking the install button.

    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_config_dialog.jpeg" width = "500" alt="Image name" align=center />


3. Click the wink button again to start the build and you can see the logs in the following window

    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_console.png" width = "500" alt="Image name" align=center />


### Configure the plugin
Add the current latest plugin address and version number to `build.gradle` in the project root path as follows

```groovy
classpath 'com.immomo.wink:plugin:0.3.23'
```

Apply the plugin to `build.gradle` in the main project `app` (you only need to apply the plugin to the main app project, not to each module):

```groovy
apply plugin: 'com.immomo.wink.plugin'
```

---

##### Configure the compilation whitelist (only compile code that has changed in the whitelist)
Configure a whitelist of directories that need to detect changes to maximize compilation speed (here we mean that we only care about changes to the module `hanisdk`, if there are multiple modules then use commas to separate them)

```groovy
winkOptions {
    moduleWhitelist = [ "hanisdk" ]
    logLevel = 4 // log level; 4 is normal, 0 opens all logs
}
```

When the configuration is done, synchronize the project, you need to run the project completely with `AS->Run` or `assembleDebug` before executing `wink` to make sure the code on the computer side and mobile side is the same.  

--

#### ARouter & EventBus annotation support (not required, on-demand dependency)
Just add winnk-compiler (reads annotations and file dependencies), currently annotationProcessor `depends on kotlin environment`.

kapt dependencies:

```groovy
kapt "com.immomo.wink:compiler-hook:0.3.23"
```
apt dependencies.
```groovy
annotationProcessor "com.immomo.wink:compiler-hook:0.3.23"
```

For different annotations, you need to create `wink_annotation_whitelist.txt` in the `.idea` directory of the project
file to identify the type of annotation to be processed (distinguished by `carriage return`, one annotation class per line)  
<br/>

Example: Processing Route annotations for ARouter -> from
[class file](https://github.com/alibaba/ARouter/blob/develop/arouter-annotation/src/main/java/com/alibaba/android/arouter/facade/annotation/Route.java)

with full path `com.alibaba.android.arouter.facade.annotation.Route`  
<br/>

The `wink_annotation_whitelist.txt` file for the AppConfig annotations for ARouter and the platform reads
```
com.alibaba.android.arouter.facade.annotation.Route
com.immomo.annotations.appconfig.appconfigv1.
```

---  


## Quick start way one: Android Studio plugin execution "Recommended, choose one of two ways
For more convenient use, you can directly use the plugin and drag it into the studio to install it:
https://s.momocdn.com/s1/u/geajgghjh/Wink-2.1.2.zip
! [image.png](/attach/60efe633bc61b.png)
Click to execute

## Quick start method 2: Script execution
### First time Wink installation requires initialization
Execute Task `winkInitWithShell`
```groovy
. /gradlew winkInitWithShell
```

### Quick start

Then make the code changes normally and execute in Terminal.

```groovy
. /wink.sh
```
The successful execution will restart the App changes to take effect!

PS: Execution by script can circumvent the time consuming Gradle initialization, the effect reflected in the Stranger App is that the time consuming of incrementing once is reduced from 10s to 3s`

### ERROR

Make sure `./gradlew installDebug` can run well, Otherwise `./gradlew wink` or `./wink.sh` task will throw an Exception.

If the above happens. Try to set `JAVA_HONE` environment as same as AndroidStudio `JDK location` path.
![WechatIMG2.png](http://tva1.sinaimg.cn/large/0020yNeuly1gv99jveyygj61au0pqqa702.jpg)


### Update log

#### 0.3.23
- Support configuring specified annotations

#### 0.3.22
- Support for compiling ARouter annotations
- Support for running multiple devices connected at the same time


#### 0.3.14i
1. Support for raw format resource files


#### 0.3.6
1. the execution is changed from Gradle Task to script, the execution efficiency is reduced from 10s to 3s
2. The whole execution system has been changed, and the winkIni initialization process has been added.

#### 0.2.3i
1. rename Litebuild to Wink
2. sort out the logs, add log colors more beautiful
3. execute error throw assert out, no longer error run the whole process
4. fix add resource not found id

## The MIT License (MIT)
The MIT License (MIT)
Copyright (c) 2021 Nasdaq: MOMO
Permission is hereby granted to any person obtaining a copy of this software and associated documentation (the "Software") to deal in it free of charge.
Copies of the Software and associated documentation files (the "Software") may be dealt with without restriction.
The Software, including but not limited to the following rights
to use, reproduce, modify, merge, publish, distribute, sublicense and/or sell copies of the Software, and to permit its use.
copies of the Software, and to permit the recipient of the Software to do so.
The recipient of the Software is permitted to do so subject to the following conditions

The above copyright notice and this permission notice shall be included in
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

the software is provided "as is" without warranty of any kind, either express or implied.
implied, including, but not limited to, the warranty of merchantability.
warranties of fitness for a particular purpose and non-infringement. In any event
In no event shall the author or copyright holder be liable for any claims, damages or other
liability, whether in contract, tort or otherwise, arising out of
arising out of, or in connection with, the Software, or any claim, damage or other liability in connection with the use of, or other dealings with, the Software, neither the author nor the copyright holder shall be liable for any
This software.
