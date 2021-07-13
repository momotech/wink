package com.immomo.wink;

import com.immomo.wink.helper.ResourceHelper;
import com.immomo.wink.helper.CompileHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.IncrementPatchHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.util.WinkLog;

public class JavaEntrance {
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            WinkLog.throwAssert("Java 命令需要指定参数：path");
            return;
        }

        WinkLog.d("====== 开始执行 Java 任务 ======");

        String path = args[0];
//        String func = args[1];

        WinkLog.d("====== path : " + path);

        InitEnvHelper helper = new InitEnvHelper();

        WinkLog.i("Wink start...");
        if (helper.isEnvExist(path)) {
            // Increment
            helper.initEnvFromCache(path);
            if (!helper.isBranchOK()) {
                new IncrementPatchHelper().fullBuildByInstallDebug(path);
                return;
            }
        } else {
            new IncrementPatchHelper().fullBuildByInstallDebug(path);
            return;
        }

        // Diff file changed
        runDiff();

        new ResourceHelper().checkResourceWithoutTask(); // 内部判断：Settings.data.hasResourceChanged
        new CompileHelper().compileCode();
        if (new IncrementPatchHelper().patchToApp()) {
            updateSnapShot();
        }
    }

    private static void updateSnapShot() {
        for (Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }

    public static boolean runDiff() {
        WinkLog.i("Diff start...");
        WinkLog.TimerLog log = WinkLog.timerStart("diff");

        for (Settings.ProjectTmpInfo projectInfo : Settings.data.projectBuildSortList) {
            WinkLog.TimerLog timerLog = WinkLog.timerStart("Diff " + projectInfo.fixedInfo.name);
            new DiffHelper(projectInfo).diff(projectInfo);

            if (projectInfo.hasResourceChanged) {
                WinkLog.i("遍历是否有资源修改, name=" + projectInfo.fixedInfo.dir);
                WinkLog.i("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                Settings.data.hasResourceChanged = true;
            }

            if (projectInfo.hasAddNewOrChangeResName) {
                Settings.data.hasResourceAddOrRename = true;
            }

            timerLog.end("name=" + projectInfo.fixedInfo.name
                    + ", changed=" + projectInfo.hasResourceChanged);
        }

        log.end("Has changed " + Settings.data.hasResourceChanged);
        return Settings.data.hasResourceChanged;
    }
}
