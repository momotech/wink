package com.immomo.wink.util

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.util.*
import java.util.jar.JarFile

class KaptEncodeUtils {
    companion object {
        @JvmStatic
        fun encodeList(options: Map<String, String>): String {
            val os = ByteArrayOutputStream()
            val oos = ObjectOutputStream(os)

            oos.writeInt(options.size)
            for ((key, value) in options.entries) {
                oos.writeUTF(key)
                oos.writeUTF(value)
            }

            oos.flush()
            val base64Str = Base64.getEncoder().encodeToString(os.toByteArray())
            println("base64Str : $base64Str")
            return base64Str
        }

        @JvmStatic
        fun hasAnnotationProcessors(file: File): Boolean {
            val processorEntryPath = "META-INF/services/javax.annotation.processing.Processor"

            try {
                when {
                    file.isDirectory -> {
                        return file.resolve(processorEntryPath).exists()
                    }
                    file.isFile && file.extension.equals("jar", ignoreCase = true) -> {
                        return JarFile(file).use { jar ->
                            jar.getJarEntry(processorEntryPath) != null
                        }
                    }
                }
            } catch (e: Exception) {
//                logger.debug("Could not check annotation processors existence in $file: $e")
            }
            return false
        }
    }


}