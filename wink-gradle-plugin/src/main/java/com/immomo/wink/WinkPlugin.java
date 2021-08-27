/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.immomo.wink;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.immomo.wink.helper.CleanupHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.tasks.WinkInitTask;
import com.immomo.wink.util.DeviceUtils;
import com.immomo.wink.util.GradleUtils;
import com.immomo.wink.util.PathUtils;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.immomo.wink.helper.InitEnvHelper.obtainAppDebugPackageName;

public class WinkPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        WinkLog.TimerLog timer = WinkLog.timerStart("apply init");

        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        appExtension.aaptOptions(aaptOptions -> {
            WinkLog.d("aaptOptions", "开始aapt配置 execute!");
            String stableIdPath = project.getRootDir() + "/.idea/" + Constant.TAG + "/stableIds.txt";
            String winkFolder = project.getRootDir() + "/.idea/" + Constant.TAG;
            File file = new File(stableIdPath);
            File lbfolder = new File(winkFolder);
            if (!lbfolder.exists()) {
                lbfolder.mkdir();
            }
            if (file.exists()) {
                WinkLog.d("aaptOptions", "开始aapt配置 execute! 文件存在  " + file.getAbsolutePath());
                aaptOptions.additionalParameters("--stable-ids", file.getAbsolutePath());
            } else {
                WinkLog.d("aaptOptions", "开始aapt配置 execute! 文件不存在");
                aaptOptions.additionalParameters("--emit-ids", file.getAbsolutePath());
            }
        });

        project.getExtensions().create("winkOptions",
                WinkOptions.class);

        project.afterEvaluate(it -> {
            WinkLog.TimerLog timerAfterEvaluate = WinkLog.timerStart("timerAfterEvaluate");
            createWinkTask(it);
            createWinkInitEnvTask(it);
            createWinkInitTask(it);
            createWinkInitWithShellTask(it);
            createCleanupTask(it);
            combineTask(it);
            timerAfterEvaluate.end();

            WinkOptions options = project.getExtensions().getByType(WinkOptions.class);
            Settings.env.options = options.copy();
            timer.end();
        });

        if (!project.getGroup().equals("wink")) {
            project.getDependencies().add("debugImplementation",
                    project.getDependencies().create("com.immomo.wink:patch-lib:0.3.22"));
        }
    }

    public void combineTask(Project project) {
        // winkCleanup depends on clean
        Task cleanUp = project.getTasks().getByName("winkCleanup");
        Task clean = project.getTasks().getByName("clean");
        clean.dependsOn(cleanUp);

        // Init wink info after packageDebug.
        Task packageDebug = GradleUtils.getFlavorTask(project, "package", "Debug");
        packageDebug.doLast(task -> afterFullBuild(project));

        // Embedded WINK_VERSION.
        GradleUtils.getFlavorTask(project, "pre", "DebugBuild").doFirst(task -> {
            Settings.data.newVersion = System.currentTimeMillis() + "";
            ((AppExtension) project.getExtensions().getByName("android"))
                    .getDefaultConfig().buildConfigField("String",
                    "WINK_VERSION", "\"" + Settings.data.newVersion + "\"");
        });
    }

    public void createWinkTask(Project project) {
        project.getTasks().register("wink", task -> {
            task.doLast(task1 -> JavaEntrance.main(new String[]{"."}));
        }).get().setGroup(Settings.NAME);
    }

    public void createWinkInitEnvTask(Project project) {
        project.getTasks().register("winkInitEnv", task ->
                task.doLast(task1 -> new InitEnvHelper().createEnv(project))
        ).get().setGroup(Settings.NAME);
    }

    public void createWinkInitTask(Project project) {
        String path = project.getRootDir().getPath();
        String version = this.getClass().getPackage().getImplementationVersion();
        String[] downloads = new String[]{String.format(Constant.DOWNLOADURL, version)};
        project.getTasks().register("winkInit", WinkInitTask.class, downloads, path, false).get().setGroup(Settings.NAME);
    }

    public void createWinkInitWithShellTask(Project project) {
        String path = project.getRootDir().getPath();
        String version = this.getClass().getPackage().getImplementationVersion();
        String[] downloads = new String[]{String.format(Constant.DOWNLOADURL, version)};
        project.getTasks().register("winkInitWithShell", WinkInitTask.class, downloads, path, true).get().setGroup(Settings.NAME);
    }

    public void createCleanupTask(Project project) {
        project.getTasks().register("winkCleanup", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    WinkLog.TimerLog timer = WinkLog.timerStart("winkCleanup", "cleanUp");
                    String rootPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME;
                    String pkgName = obtainAppDebugPackageName(project);
                    // 清理
                    new CleanupHelper(pkgName, rootPath).cleanOnAssemble();
                    new com.immomo.wink.helper.CleanupHelper(pkgName, rootPath).cleanup();
                    timer.end("cleanUp");
                }
            });
        }).get().setGroup(Settings.NAME);
    }

    private void afterFullBuild(Project project) {
        String rootPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME;
        String pkgName = obtainAppDebugPackageName(project);
        // 清理
        new CleanupHelper(pkgName, rootPath).cleanOnAssemble();
        // 初始化
        new InitEnvHelper().initEnv(project, true);
        // 产生快照
        DiffHelper.initAllSnapshot();

        cacheApkFile(project);

        createKotlinFile(project);

        pushVersionFileToDevice(project);
    }

    // 创建 Kotlin 文件，用来 kapt 编译
    private void createKotlinFile(Project project) {
        String fileName = project.getRootDir() + "/.idea/" + Settings.NAME + "/KaptCompileFile.kt";
        String fileContent =
            "package com.immomo.wink.patch\n" +
            "class KaptCompileFile {\n" +
            "}";

        byte[] sourceByte = fileContent.getBytes();
        if (null != sourceByte) {
            try {
                File file = new File(fileName);        //文件路径（路径+文件名）
                if (!file.exists()) {    //文件不存在则创建文件，先创建目录
                    File dir = new File(file.getParent());
                    dir.mkdirs();
                    file.createNewFile();
                }
                FileOutputStream outStream = new FileOutputStream(file);    //文件输出流用于将数据写入文件
                outStream.write(sourceByte);
                outStream.close();    //关闭文件输出流
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 推送本次打包 Version.png 到设备
    private void pushVersionFileToDevice(Project project) {
        WinkLog.d("Settings.env.version : " + Settings.env.version + " --- Settings.data.newVersion : " + Settings.data.newVersion);

        List<String> connectingDevices = DeviceUtils.getConnectingDevices();

        for (String deviceId : connectingDevices) {
            String localVersionFilePath = project.getRootDir() + "/.idea/" + Settings.NAME + "/version/";
            File file = new File(localVersionFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            deleteDir(file, false);
            File versionFile = new File(localVersionFilePath + "/" + Settings.env.version + ".png");
            try {
                versionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String versionPath = PathUtils.getVersionPath(deviceId);
            Utils.runShells("source ~/.bash_profile\n" +
                    "adb -s " + deviceId + " shell rm -rf " + versionPath + "\n" +
                    "adb -s " + deviceId + " shell mkdir " + versionPath + "\n" +
                    "adb -s " + deviceId + " push " + versionFile.getAbsolutePath() + " " + versionPath);
        }
    }

    public void deleteDir(File file, boolean deleteSelf) {
        if (file.isDirectory()) {
            for (File f : file.listFiles())
                deleteDir(f, true);
        }
        if (deleteSelf) {
            file.delete();
        }
    }

    //copy apk to wink dir
    private void cacheApkFile(Project project) {
        boolean hasAppPlugin = project.getPlugins().hasPlugin("com.android.application");
        if (hasAppPlugin) {
            System.out.println("该module未包含com.android.application插件");
            AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
            for (ApplicationVariant variant : androidExt.getApplicationVariants()) {
                if (variant.getName().equals("debug")) {
                    variant.getOutputs().all(baseVariantOutput -> {
                        File srcFile = baseVariantOutput.getOutputFile();
                        String winkPath = project.getRootDir() + "/" + Constant.IDEA + "/" + Constant.TAG;
                        File destFile = new File(winkPath, Constant.TEMP_APK_NAME);
                        if (destFile.exists()) {
                            destFile.delete();
                        }
                        try {
                            Files.copy(srcFile.toPath(), destFile.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    private void updateSnapShot() {
        for (Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new com.immomo.wink.helper.DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }

    private String[] getCurrentJarUrl() {
        String version = this.getClass().getPackage().getImplementationVersion();
        String url = String.format(Constant.DOWNLOADURL, version);
        return new String[]{url};
    }

}