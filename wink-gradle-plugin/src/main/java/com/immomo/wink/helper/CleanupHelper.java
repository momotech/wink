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

import com.immomo.wink.Constant;
import com.immomo.wink.Settings;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;

import java.io.File;

import static com.immomo.wink.helper.InitEnvHelper.obtainAppDebugPackageName;

public class CleanupHelper {

    String mRootPath;
    String mPackageName;

    public CleanupHelper(String packageName, String rootPath) {
        mPackageName = packageName;
        mRootPath = rootPath;
    }

    public void cleanup() {
        deleteAllApk();
        deleteAllDex();
        delete("diff");
        delete("tmp_class");
        delete("env");
        delete("stableIds.txt");
        delete("tmp_class.zip");

        // 删除手机上的patch文件
        deletePatchFileOnPhone();
    }

    public void cleanOnAssemble() {
        deleteAllApk();
        deleteAllDex();

        delete("diff");
        delete("tmp_class");
        delete("env");
        delete("tmp_class.zip");

        // 删除手机上的patch文件
        deletePatchFileOnPhone();
    }


    public void deletePatchFileOnPhone() {
        String debugPackageName = Settings.env.debugPackageName;
        if (TextUtils.isEmpty(Settings.env.debugPackageName) && !TextUtils.isEmpty(mPackageName)) {
            debugPackageName = mPackageName;
            WinkLog.d("deletePatchFileOnPhone env is empty.");
        }
        WinkLog.d("deletePatchFileOnPhone debugPackageName=" + debugPackageName);
        String destPath = "/sdcard/Android/data/" + debugPackageName + "/patch_file/";
        String destPath2 = "/sdcard/" + Constant.TAG + "/patch_file/";

        String cmds = "";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "adb shell rm -rf " + destPath;
        cmds += '\n' + "adb shell mkdir " + destPath;

        cmds += '\n' + "adb shell rm -rf " + destPath2;
        Utils.runShells(cmds);
    }

    public void deleteAllApk() {
        WinkLog.d("deleteAllApk :" + mRootPath);
        String path = mRootPath;
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            File[] apks = f.listFiles(pathname -> pathname.getName().endsWith("apk"));
            if (apks != null) {
                for (File a : apks) {
                    a.delete();
                }
            }
        }
    }

    public void deleteAllDex() {
        WinkLog.d("deleteAllDex :" + mRootPath);
        String path = mRootPath;
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles(pathname -> (pathname.getName().endsWith("dex") || pathname.getName().endsWith("jar")));
            if (files != null) {
                for (File a : files) {
                    a.delete();
                }
            }
        }
    }

    public void delete(String path) {
        WinkLog.d("delete file :" + mRootPath + "/" + path);
        File f = new File(mRootPath + "/" + path);
        deleteFile(f);
    }

    public boolean deleteFile(File dirFile) {
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        return dirFile.delete();
    }
}