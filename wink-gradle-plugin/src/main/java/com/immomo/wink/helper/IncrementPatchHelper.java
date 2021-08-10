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

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.immomo.wink.Constant.RESOURCE_APK_SUFFIX;

public class IncrementPatchHelper {
    public boolean patchToApp() {
        if (Settings.data.classChangedCount <= 0 && !Settings.data.hasResourceChanged) {
            WinkLog.i("No changed, nothing to do.");
            return false;
        }

        WinkLog.d("[IncrementPatchHelper]->[patchToApp] \n是否有资源变动：" + Settings.data.classChangedCount + "，是否新增改名：" + Settings.data.hasResourceChanged);

        List<String> devicesList = getConnectingDevices();

        createPatchFile(devicesList);
        patchDex(devicesList);
        patchResources(devicesList);
        restartApp(devicesList);

        WinkLog.i("Patch finish in " + (System.currentTimeMillis() - Settings.data.beginTime) / 1000 + "s.");
        WinkLog.i(Settings.data.classChangedCount + " file changed, "
                + (Settings.data.hasResourceChanged ? "has" : "no") + " resource changed.");
        return true;
    }

    @NotNull
    private List<String> getConnectingDevices() {
        List<String> devicesList = new ArrayList<>();

        String cmds = "";
        cmds += " source ~/.bash_profile";
        cmds += '\n' + "adb devices ";

        Utils.ShellResult shellResult = Utils.runShells(cmds);
        List<String> resultList = shellResult.getResult();
        if (resultList == null || resultList.size() <= 1) { // 只有一个设备或没有设备
            WinkLog.throwAssert("USB 没有连接到设备");
        } else {
            List<String> devices = resultList.subList(1, resultList.size());
            System.out.println("adb_devices 111 : " + devices.toString());
            for (String deviceStr : devices) {
                String [] arr = deviceStr.split("\\s+");
                System.out.println(arr[0]);
//                Utils.runShells("adb -s " + arr[0] + " install ");
                devicesList.add(arr[0]);
            }
        }
        return devicesList;
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

        List<String> connectingDevices = getConnectingDevices();
        for (String connectingDevice : connectingDevices) {
            Utils.runShells(Utils.ShellOutput.ALL, "cd " + path,
                    "source ~/.bash_profile",
                    "adb -s " + connectingDevice + " shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity);
        }

        WinkLog.i("Full build finish in " + (System.currentTimeMillis() - beginTime) / 1000 + "s.");
    }
}