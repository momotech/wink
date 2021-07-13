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

package com.immomo.wink.helper;

import com.immomo.wink.Settings;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;

import static com.immomo.wink.Constant.RESOURCE_APK_SUFFIX;

public class IncrementPatchHelper {
    public boolean patchToApp() {
        if (Settings.data.classChangedCount <= 0 && !Settings.data.hasResourceChanged) {
            WinkLog.i("No changed, nothing to do.");
            return false;
        }

        WinkLog.d("[IncrementPatchHelper]->[patchToApp] \n是否有资源变动：" + Settings.data.classChangedCount + "，是否新增改名：" + Settings.data.hasResourceChanged);

        createPatchFile();
        patchDex();
        patchResources();
        restartApp();

        WinkLog.i("Patch finish in " + (System.currentTimeMillis() - Settings.data.beginTime) / 1000 + "s.");
        WinkLog.i(Settings.data.classChangedCount + " file changed, "
                + (Settings.data.hasResourceChanged ? "has" : "no") + " resource changed.");
        return true;
    }

    public void createPatchFile() {
        String patch = "/sdcard/Android/data/" + Settings.env.debugPackageName;
        Utils.ShellResult result = Utils.runShells("source ~/.bash_profile\nadb shell ls " + patch);
        boolean noPermission = false;
        Utils.runShells(Utils.ShellOutput.NONE, "adb shell mkdir " + patch);
        for (String error : result.getErrorResult()) {
            if (error.contains("Permission denied")) {
                // 标志没文件权限
                noPermission = true;
                break;
            }
        }

        if (noPermission) {
            Settings.data.patchPath = "/sdcard/" + Settings.NAME + "/patch_file/";
        } else {
            Settings.data.patchPath = "/sdcard/Android/data/" + Settings.env.debugPackageName + "/patch_file/";
        }

        Utils.runShells(Utils.ShellOutput.NONE, "source ~/.bash_profile",
                "adb shell mkdir " + Settings.data.patchPath);

        result = Utils.runShells("adb shell ls " + Settings.data.patchPath);
        if (result.getErrorResult().size() > 0) {
            WinkLog.throwAssert("Can not create patch file " + Settings.data.patchPath);
        }
    }

    public void patchDex() {
        if (Settings.data.classChangedCount <= 0) {
            return;
        }

        WinkLog.i("Dex patching...");

        String patchName = Settings.env.version + "_patch.jar";
        Utils.runShells("source ~/.bash_profile\n" + "adb push " + Settings.env.tmpPath + "/" + patchName
                + " " + Settings.data.patchPath + Settings.env.version + "_patch.png");
    }

    public void patchResources() {
        if (!Settings.data.hasResourceChanged) {
            return;
        }

        WinkLog.i("Resources patching...");

        String patchName = Settings.env.version + ResourceHelper.apk_suffix;
        Utils.runShells("source ~/.bash_profile\n" +
                "adb shell rm -rf " + Settings.data.patchPath + "apk\n" +
                "adb shell mkdir " + Settings.data.patchPath + "apk\n" +
                "adb push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath + "apk/" +
                Settings.env.version + RESOURCE_APK_SUFFIX);
    }

    public void restartApp() {
        String cmds = "";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "adb shell am force-stop " + Settings.env.debugPackageName;
        cmds += '\n' + "adb shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity;
        Utils.runShells(cmds);
    }

    public void fullBuildByInstallDebug(String path) {
        WinkLog.i("Cache or Branch invalid, start full build...");
        long beginTime = System.currentTimeMillis();
        Utils.runShells(Utils.ShellOutput.ALL, "cd " + path + " && " + "./gradlew installDebug");

        // 初始化数据
        new InitEnvHelper().initEnvFromCache(path);

        Utils.runShells(Utils.ShellOutput.ALL, "cd " + path,
                "source ~/.bash_profile",
                "adb shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity);

        WinkLog.i("Full build finish in " + (System.currentTimeMillis() - beginTime) / 1000 + "s.");
    }
}