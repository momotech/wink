package com.immomo.wink.helper

import com.immomo.wink.Constant
import com.immomo.wink.Settings
import com.immomo.wink.util.Utils
import com.immomo.wink.util.WinkLog
import java.io.File
import java.io.FileNotFoundException

class ResourceHelper {
    companion object {
        const val apk_suffix = "_resources-debug.apk"
    }
    var st : Long = 0

    fun checkResource() {
        WinkLog.d(" \n ResourceHelper process, changed=${Settings.data.hasResourceChanged}")
        if (!Settings.data.hasResourceChanged) return

        WinkLog.i("Process resources...")

        st = System.currentTimeMillis()
        compileResources()
    }

    private fun compileResources() {
        val stableId = File(Settings.env.tmpPath + "/stableIds.txt")
        if (stableId.exists()) {
            WinkLog.d("stableIds file exist!")
        } else {
            WinkLog.d("=================================")
            throw FileNotFoundException("stableIds not exist! Please compile project completely first.")
        }

        Settings.data.needProcessDebugResources = true
    }

    fun checkResourceWithoutTask() {
        WinkLog.d(" \n ResourceHelper process, changed=${Settings.data.hasResourceChanged}")
        if (!Settings.data.hasResourceChanged) return

        WinkLog.i("Process resources...")

        st = System.currentTimeMillis()
        compileResourcesWithout()
        packageResources()
    }

    private fun compileResourcesWithout() {
        val stableId = File(Settings.env.tmpPath + "/stableIds.txt")
        if (stableId.exists()) {
            WinkLog.d("stableIds file exist!")
        } else {
            WinkLog.d("=================================")
            throw FileNotFoundException("stableIds not exist! Please compile project completely first.")
        }

        Settings.data.needProcessDebugResources = true
        val ret = Utils.runShells(Utils.ShellOutput.ALL, "cd ${Settings.env.appProjectDir}/../", "./gradlew process${Utils.upperCaseFirst(Settings.env.defaultFlavor)}DebugResources --offline")
        if (ret.errorResult.size > 0) {
//            WinkLog.throwAssert("Compile resources error.");
        }
    }

    var ap_path = ""
    fun findAp_(file: File) {
        if (file.isFile) {
            WinkLog.d("findAp_ is file=${file.name}")
            if (file.name.endsWith(".ap_")) {
                WinkLog.d("is ap_ file=${file.name}")
                ap_path = file.absolutePath
            }
        }
        if (file.isDirectory) {
            for (f : File in file.listFiles()) {
                WinkLog.d("recursive find ap_ f=${f.name}")
                findAp_(f)
            }
        }
    }

    fun packageResources() {
        val ap_ParentDir = File("${Settings.env.appProjectDir}/build/intermediates/processed_res")
        findAp_(ap_ParentDir)

        WinkLog.d("packageResources-packageResources rootPath=====${ap_ParentDir.absolutePath}")
        WinkLog.d("find ap_ file: ${ap_path}")

        if (!ap_path.isBlank()) {
            WinkLog.d("ap_ file exist.")
        } else {
            throw FileNotFoundException("ap_ file not exist!")
        }

        val lastPath = Settings.env.rootDir
        val winkFolderPath = Settings.env.tmpPath
        val patchName = Settings.env.version + apk_suffix
        val apkPath = "$winkFolderPath/$patchName"
        val pushSdcardPath = "/sdcard/Android/data/${Settings.env.debugPackageName}/patch_file/apk"

        WinkLog.d("Resource package path: $apkPath")
        val app = Settings.env.projectTreeRoot!!.name
        val localScript = """
            source ~/.bash_profile
            echo "Start unzip, zip resource ap_ !"
            rm -rf $lastPath/.idea/${Constant.TAG}/tempResFolder
            mkdir $lastPath/.idea/${Constant.TAG}/tempResFolder
            unzip -o -q ${ap_path} -d $lastPath/.idea/${Constant.TAG}/tempResFolder
            cp -R $lastPath/$app/build/intermediates/merged_assets/${Settings.env.variantName}/out/. $lastPath/.idea/${Constant.TAG}/tempResFolder/assets
            cd $lastPath/.idea/${Constant.TAG}/tempResFolder
            zip -r -o -q $apkPath *
            cd ..
            rm -rf tempResFolder
        """.trimIndent()

        Utils.runShells(localScript)

        WinkLog.d("Resource full build and package cost: " + (System.currentTimeMillis() - st))
    }



}