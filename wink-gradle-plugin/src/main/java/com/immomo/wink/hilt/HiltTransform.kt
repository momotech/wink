package com.immomo.wink.hilt

import com.immomo.wink.Settings
import java.io.File

object HiltTransform{

    private val srcPath = File(Settings.env.tmpPath + "/tmp_class")

    private val transformer by lazy {
        val transformPath = File("/Users/weixin/Documents/litebuild/sunflower/app/build/intermediates/transforms/AndroidEntryPointTransform/debug/")
        val inputFiles: List<File> = transformPath.listFiles().toList()
        AndroidEntryPointClassTransformer("liteBuild", inputFiles, srcPath, true)
    }

    fun transform() {
        srcPath.walk().filter { it.extension == "class" }.forEach {
            transformer.transformFile(it)
        }
    }
}