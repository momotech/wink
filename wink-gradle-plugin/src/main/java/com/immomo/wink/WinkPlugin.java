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
import com.immomo.wink.helper.CleanupHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.tasks.WinkInitTask;
import com.immomo.wink.util.GradleUtils;
import com.immomo.wink.util.WinkLog;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

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
                    project.getDependencies().create("com.immomo.wink:patch-lib:0.2.0i"));
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