简体中文 | [English](./README.md)

## Wink

Wink是AndroidStudio的一个快速构建插件。Wink的目标是建立一个易于使用和易于维护的Android快速构建插件。我们与其他快速构建插件的区别在于，我们的快速构建过程完全使用Gradle和Java语言实现，支持快速编译java文件、kotlin文件和资源变化。


## Wink Insight 
在Android开发过程中，应用程序的构建速度直接影响到开发者一天的工作效率。对于已经维护了几年的大型安卓项目来说，一行代码的改变有可能需要10分钟以上的时间来编译。虽然Instant Run可以带来一些优化，但优化结果仍然是分钟级的，Wink就是为了解决这个问题而产生的，对于简单的代码修改，编译速度提高到秒级。基于Gradle和Groovy，我们对java、kotlin和资源文件的编译过程进行了优化，并将整个过程交给Gradle的Task系统，不使用其他脚本语言，这不仅保证了项目代码的可读性和易用性，也大大提高了代码的可维护性。

## 特点 
1. 支持Java变更
2. 支持kotlin变更
3. Gradle mutil模块支持
4. 支持资源变更


## 安装 
### 通过AndroidStuido插件安装
1. 从[wink_studio_plugin.zip]下载插件(https://s.momocdn.com/s1/u/geajgghjh/Wink-2.1.2.zip)
2. 打开AndroidStudio 
3. 将Wink.zip拖到AndroidStudio 
4. 重新启动AndroidStudio

## 由AndroidStudio插件运行
1. 当你安装插件成功后。在你打开一个项目后，你可以看到wink按钮，就像这个屏幕截图。


2. 点击wink按钮，你可以看到这样的对话框。选择项目根目录的build.gradle文件和主应用程序build.gradle文件。通过点击安装按钮自动改变gradle文件。

    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_config_dialog.jpeg" width = "500"  alt="图片名称" align=center />


3. 再次点击wink按钮，开始编译，你可以如下窗口看到相关日志

    <img src="https://s.momocdn.com/s1/u/dcehhhadi/sh_console.png" width = "500"  alt="图片名称" align=center />


### 配置插件
在项目根路径的 `build.gradle` 中加入当前最新的插件地址和版本号如下：

```groovy
classpath 'com.immomo.wink:plugin:0.3.27'
```

在主工程 `app` 中的 `build.gradle` 应用该插件（只需在主 app 工程中应用插件即可，无需每个 module 都应用）:

```groovy
apply plugin: 'com.immomo.wink.plugin'
```

---

##### 配置编译白名单（只编译白名单中变更的代码）
配置需要检测变更的白名单目录，可以更最大程度提高编译速度（这里表示我们只关心 `hanisdk` 该 module 的文件变更，若有多个 module 则用逗号分隔）：

```groovy
winkOptions {
    moduleWhitelist = [ "hanisdk" ]
    logLevel = 4 // 日志等级；4为普通，0打开所有日志
}
```

当配置完成后，同步项目，在执行 `wink` 之前需要先完整用 `AS->Run` 或者 `assembleDebug` 完整运行一遍项目，保证电脑端和手机端代码一致。  

---

#### ARouter & EventBus 注解支持（非必须，按需依赖）
添加 winnk-compiler (读取注解与文件的依赖关系)即可，目前 annotationProcessor `依赖 kotlin 环境`

kapt 依赖:
```groovy
kapt "com.immomo.wink:compiler-hook:0.3.23"
```
apt 依赖：
```groovy
annotationProcessor "com.immomo.wink:compiler-hook:0.3.23"
```

针对不同的注解，需要在项目的 `.idea` 目录下创建 `wink_annotation_whitelist.txt`
文件来标识需要处理的注解类型（用`回车`区分，每行一个注解类）  
<br/>

例如：处理 ARouter 的 Route 注解 -> 从[类文件](https://github.com/alibaba/ARouter/blob/develop/arouter-annotation/src/main/java/com/alibaba/android/arouter/facade/annotation/Route.java) 中找到完整路径为`com.alibaba.android.arouter.facade.annotation.Route`  
<br/>

ARouter 和平台的 AppConfig 注解的 `wink_annotation_whitelist.txt` 文件内容如下
```
com.alibaba.android.arouter.facade.annotation.Route
com.immomo.annotations.appconfig.appconfigv1.AppConfigV1
```

---  


## 快速启动方式一：Android Studio插件执行 「推荐，两种方式二选一」
为了更方便的使用，可以直接用插件，拖进studio即可安装:
https://s.momocdn.com/s1/u/geajgghjh/Wink-2.1.2.zip
![image.png](/attach/60efe633bc61b.png)
点击即可执行

## 快速启动方式二：脚本执行
### 首次安装Wink需初始化
执行 Task `winkInitWithShell`
```groovy
./gradlew winkInitWithShell
```

### 快速启动

然后正常进行代码的修改，在Terminal中执行：

```groovy
./wink.sh
```
执行成功后会重启App变更生效！

`PS：用脚本执行能规避Gradle初始化的耗时，在陌陌App中体现的效果是增量一次的耗时由10s降低到3s`

### 集成异常处理

需要确保在终端执行 `./gradlew installDebug` 可以正常运行，否则在未执行全量包时，执行 `./gradlew wink` 或 `./wink.sh` 会报错。

如果出现异常，可以尝试配置 `JAVA_HONE` 路径与 AndroidStudio `JDK location` 路径一致。
![WechatIMG2.png](http://tva1.sinaimg.cn/large/0020yNeuly1gv99jveyygj61au0pqqa702.jpg)

### 打包到 Maven
- 首先执行 `assemble` Task
- 打开 `wink-patch-lib` 目录下的 `apply from: 'repo-maven-push.gradle'`
- 打开 `wink` 目录下的 `settings.gradle` 的 `include ':wink-gradle-plugin'`
- 更新 `repo-maven-push.gradle` 和 `:wink-gradle-plugin` 的 `build.gradle` 的 Version 
- 注释 `buildSrc` 目录下的 `settings.gradle` 的代码，打开 `wink-gradle-plugin` 目录下 `build.gradle` 中 `afterEvaluate` 代码块

### 更新日志

#### 0.3.23
- 支持配置指定注解

#### 0.3.22
- 支持编译 ARouter 注解
- 支持同时连接多个设备运行


#### 0.3.14i
1. 支持raw格式资源文件


#### 0.3.6
1. 执行由Gradle Task变更为脚本，执行效率由10s降低到3s
2. 由此而来整个执行体系变更，增加winkIni初始化流程

#### 0.2.3i
1. Litebuild重命名为Wink
2. 梳理日志，增加了日志颜色更美观了
3. 执行错误抛assert出来，不再错误的跑完全程
4. fix 新增资源找不到id

## The MIT License (MIT)
```
Copyright (c) 2021 纳斯达克：MOMO
特此允许任何获得本软件及相关文档文件（"软件"）副本的人免费进行交易。
本软件和相关文档文件（"软件"）的副本，可以不受限制地处理本软件。
本软件，包括但不限于以下权利
使用、复制、修改、合并、出版、分发、分许可和/或销售本软件的副本，并允许其使用本软件。
软件的副本，并允许接受软件的人这样做。
在符合下列条件的情况下，允许接受软件的人这样做。

上述版权声明和本许可声明应包括在
上述版权声明和本许可声明应包括在本软件的所有副本或实质部分中。

本软件 "按原样 "提供，不提供任何形式的明示或暗示的保证。
暗示的，包括但不限于适销性的保证。
对某一特定目的的适用性和不侵权的保证。在任何情况下
作者或版权持有人在任何情况下都不对任何索赔、损害或其他责任负责。
责任，无论是在合同、侵权行为或其他方面的诉讼中，由以下原因引起。
引起的，或与本软件有关的，或与本软件的使用或其他交易有关的任何索赔、损害赔偿或其他责任，作者或版权持有人均不承担任何责任。
本软件。
```
    
