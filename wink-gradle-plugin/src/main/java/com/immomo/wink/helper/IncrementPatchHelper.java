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
import com.immomo.wink.util.DeviceUtils;
import com.immomo.wink.util.PathUtils;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import static com.immomo.wink.Constant.RESOURCE_APK_SUFFIX;

public class IncrementPatchHelper {
    public boolean patchToApp() {
        List<String> devicesList = DeviceUtils.getConnectingDevices();
        if (Settings.data.classChangedCount <= 0 && !Settings.data.hasResourceChanged) {
            WinkLog.i("No changed, nothing to do.");
            installAppIfDiffVersion(devicesList);
            return false;
        }

        WinkLog.d("[IncrementPatchHelper]->[patchToApp] \n是否有资源变动：" + Settings.data.classChangedCount + "，是否新增改名：" + Settings.data.hasResourceChanged);

        createPatchFile(devicesList);
        patchDex(devicesList);
        patchResources(devicesList);
        restartApp(devicesList);

        WinkLog.i("Patch finish in " + (System.currentTimeMillis() - Settings.data.beginTime) / 1000 + "s.");
        WinkLog.i(Settings.data.classChangedCount + " file changed, "
                + (Settings.data.hasResourceChanged ? "has" : "no") + " resource changed.");
        return true;
    }

    // 检测手机中 Version 是否与本地相同，不同则 push 缓存 apk 和 Version 文件
    private void installAppIfDiffVersion(List<String> devicesList) {
//        WinkLog.i("installAppIfDiffVersion run !!!");
        for (String deviceId : devicesList) {
//            WinkLog.i("installAppIfDiffVersion --- deviceId : " + deviceId + " === versionPath : " + PathUtils.getVersionPath(deviceId));
            boolean diffVersion = isDiffVersion(deviceId);
            if (diffVersion) {
                installLocalApk_Version(deviceId);
            }
        }
    }

    /**
     * 安装本地 Apk 并更新 Version 文件
     */
    private void installLocalApk_Version(String deviceId) {
        WinkLog.i("与本机打包 Apk 版本不一致，安装本地版本 Apk 与补丁包");
        Utils.runShells("adb -s " + deviceId + " install " + Settings.env.tmpPath + "/temp.apk");

        String versionPath = PathUtils.getVersionPath(deviceId);

        String localVersionPath = Settings.env.tmpPath + "/version/" + Settings.env.version + ".png";
        Utils.runShells("source ~/.bash_profile\n" +
                "adb -s " + deviceId + " shell rm -rf " + versionPath + "\n" +
                "adb -s " + deviceId + " shell mkdir " + versionPath + "\n" +
                "adb -s " + deviceId + " push " + localVersionPath + " " + versionPath);
    }

    private boolean isDiffVersion(String deviceId) {
        boolean diffVersion = true;
        Utils.ShellResult versionResult = Utils.runShells("adb -s " + deviceId + " shell ls " + PathUtils.getVersionPath(deviceId));
        for (String s : versionResult.getResult()) {
            if (s.contains(Settings.env.version)) {
                diffVersion = false;
                break;
            }
        }
        return diffVersion;
    }

    public void createPatchFile(@NotNull List<String> devicesList) {
        for (int i = 0; i < devicesList.size(); i++) {
            String deviceId = devicesList.get(i);
            String patch = "/sdcard/Android/data/" + Settings.env.debugPackageName;
            Utils.ShellResult result = Utils.runShells("source ~/.bash_profile\nadb -s " + deviceId + "shell ls " + patch);
            boolean noPermission = false;
            Utils.runShells(Utils.ShellOutput.NONE, "adb -s " + deviceId + "shell mkdir " + patch);
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
                    "adb -s " + deviceId + " shell mkdir " + Settings.data.patchPath);

            result = Utils.runShells("adb -s " + deviceId + " shell ls " + Settings.data.patchPath);
            if (result.getErrorResult().size() > 0) {
                WinkLog.throwAssert("Can not create patch file " + Settings.data.patchPath);
            }
        }
    }

    public void patchDex(@NotNull List<String> devicesList) {
        if (Settings.data.classChangedCount <= 0) {
            return;
        }

        WinkLog.i("Dex patching...");

        for (int i = 0; i < devicesList.size(); i++) {
            String patchName = Settings.env.version + "_patch.jar";
            Utils.runShells("source ~/.bash_profile\n" + "adb -s " + devicesList.get(i) + " push " + Settings.env.tmpPath + "/" + patchName
                    + " " + Settings.data.patchPath + Settings.env.version + "_patch.png");
        }

    }

    public void patchResources(@NotNull List<String> devicesList) {
        if (!Settings.data.hasResourceChanged) {
            return;
        }

        WinkLog.i("Resources patching...");

        for (String deviceId : devicesList) {
            String patchName = Settings.env.version + ResourceHelper.apk_suffix;
            Utils.runShells("source ~/.bash_profile\n" +
                    "adb -s " + deviceId + " shell rm -rf " + Settings.data.patchPath + "apk\n" +
                    "adb -s " + deviceId + " shell mkdir " + Settings.data.patchPath + "apk\n" +
                    "adb -s " + deviceId + " push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath + "apk/" +
                    Settings.env.version + RESOURCE_APK_SUFFIX);
        }
    }

    public void restartApp(@NotNull List<String> devicesList) {
        for (String deviceId : devicesList) {
            if (isDiffVersion(deviceId)) {
                installLocalApk_Version(deviceId);
                continue;
            }
            String cmds = "";
            cmds += "source ~/.bash_profile";
            cmds += '\n' + "adb -s " + deviceId + " shell am force-stop " + Settings.env.debugPackageName;
            cmds += '\n' + "adb -s " + deviceId + " shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity;
            Utils.runShells(cmds);
        }
    }

    public void fullBuildByInstallDebug(String path) {
        WinkLog.i("Cache or Branch invalid, start full build...");
        long beginTime = System.currentTimeMillis();
        Utils.runShells(Utils.ShellOutput.ALL, "cd " + path + " && " + "./gradlew installDebug");

        // 初始化数据
        new InitEnvHelper().initEnvFromCache(path);

        List<String> connectingDevices = DeviceUtils.getConnectingDevices();
        for (String connectingDevice : connectingDevices) {
            Utils.runShells(Utils.ShellOutput.ALL, "cd " + path,
                    "source ~/.bash_profile",
                    "adb -s " + connectingDevice + " shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity);
        }

        WinkLog.i("Full build finish in " + (System.currentTimeMillis() - beginTime) / 1000 + "s.");
    }
}