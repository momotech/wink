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



### 用Gradle安装
1 . 在你的项目根目录下找到你的 build.gradle，并在依赖项块中添加一行 classpath 'com.immomo.litebuild:plugin:0.1.45' 

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
2 .在你的主应用程序文件夹中找到build.gradle文件。插入行插件配置，如演示代码
```
plugins {
    id 'com.android.application'
    id 'com.immomo.litebuild.plugin'
    id 'kotlin-android'
}
```
或者这样

    apply plugin: 'com.android.application'
    apply plugin: 'com.immomo.litebuild.plugin'


##通过Gradle或终端运行
1. 安装插件后，你可以通过Wink按钮运行或通过gradle任务列表中的gradle任务litebuild运行

    <img src="https://s.momocdn.com/s1/u/dcehhhadi/gradle_task_lite_build.png" width = "330" height = "305" alt="图片名称" align=center />


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

通过www.DeepL.com/Translator（免费版）翻译
```
    
