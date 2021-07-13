//package com.immomo.wink
//
//import com.android.build.api.transform.*
//import com.android.build.gradle.internal.pipeline.TransformManager
//import com.immomo.wink.util.WinkLog
//import jdk.internal.org.objectweb.asm.*
//import org.gradle.api.Project
//import java.io.File
//import java.io.FileInputStream
//import java.io.InputStream
//
//
//class WinkAsmTransform(val project: Project, val config: Any?) : Transform() {
//    override fun getName(): String {
//        return "WinkAsmTransform"
//    }
//
//    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
//        TransformManager.CONTENT_CLASS
//
//    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
//        TransformManager.SCOPE_FULL_PROJECT
//
//    override fun isIncremental(): Boolean = true
//
//    override fun transform(transformInvocation: TransformInvocation?) {
//        WinkLog.vNoLimit("[WinkAsmTransform] transform... transformInvocation")
//        val includeModules = config?.toString()?.split(",") ?: emptyList()
//
//        val asmScanner = AsmScanner()
//        transformInvocation?.inputs?.forEach { input ->
//            WinkLog.vNoLimit("[WinkAsmTransform] intput... ${input}")
//            input.directoryInputs.forEach { dirInput ->
//                val outputDir = transformInvocation.outputProvider.getContentLocation(
//                    dirInput.name,
//                    dirInput.contentTypes,
//                    dirInput.scopes,
//                    Format.DIRECTORY
//                )
//                asmScanner.handleDirInput(dirInput, outputDir)
//            }
//        }
//    }
//
//}
//
//class AsmScanner() {
//    fun handleDirInput(dirInput: DirectoryInput, outputDir: File) {
//        val inputFilePathLen = dirInput.file.path.length
//        val copyAndInspect = { inputFile: File, inputFileStatus: Status, needCopy: Boolean ->
//            val outputFile = File(outputDir, inputFile.path.substring(inputFilePathLen))
//            this.inspectFile(inputFile, inputFileStatus, outputFile)
//            if (needCopy) {
//                when (inputFileStatus) {
//                    Status.ADDED, Status.CHANGED -> {
//                        if (inputFile.isDirectory) {
//                            outputFile.mkdirs()
//                        } else {
//                            inputFile.copyTo(outputFile, overwrite = true)
//                        }
//                    }
//                    Status.REMOVED -> {
//                        outputFile.deleteRecursively()
//                    }
//                    else -> Unit
//                }
//            }
//        }
//        dirInput.changedFiles.forEach { inputFile, inputFileStatus ->
//            copyAndInspect(inputFile, inputFileStatus, true)
//        }
//    }
//
//    fun inspectFile(inputFile: File, inputFileStatus: Status, outputFile: File) =
//        when (inputFileStatus) {
//            Status.ADDED, Status.CHANGED -> {
//                if (inputFile.isFile && inputFile.name.endsWith(".class")) inputFile else null
//            }
//            Status.REMOVED -> {
//                if (outputFile.isFile && outputFile.name.endsWith(".class")) outputFile else null
//            }
//            else -> null
//        }?.let { file ->
//            FileInputStream(file).readClass {
////                appAsmSource = AppAsmSource.FileSource(
////                    file.absolutePath,
////                    outputFilePath = outputFile.absolutePath
////                )
//            }
//        }
//
//    fun doProcess() {
//        val classReader =
//            ClassReader("/Users/momo/projects/litebuild/wink/wink-demo-app/build/intermediates/javac/debug/classes/com/immomo/wink/MainActivity3")
//        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//        classReader.accept(object : ClassVisitor(Opcodes.ASM5, classWriter) {
//            override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
//                return AnnotationPrinterVisitor(cv.visitAnnotation(descriptor, visible))
//            }
//        }, ClassReader.SKIP_DEBUG)
//    }
//}
//
//class AnnotationPrinterVisitor(annotationVisitor: AnnotationVisitor) :
//    AnnotationVisitor(Opcodes.ASM5, annotationVisitor) {
//
//    override fun visitEnd() {
//        super.visitEnd()
//        WinkLog.vNoLimit("visitEnd")
//    }
//
//    override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
//        WinkLog.vNoLimit("visitAnnotation, name = $name, descriptor = $descriptor")
//        return super.visitAnnotation(name, descriptor)
//    }
//
//    override fun visitEnum(name: String?, descriptor: String?, value: String?) {
//        WinkLog.vNoLimit("visitEnum, name = $name, descriptor = $descriptor, value = $value")
//        super.visitEnum(name, descriptor, value)
//    }
//
//    override fun visit(name: String?, value: Any?) {
//        super.visit(name, value)
//        WinkLog.vNoLimit("visit, name = $name, value = $value")
//    }
//
//    override fun visitArray(name: String?): AnnotationVisitor {
//        WinkLog.vNoLimit("visitArray, name = $name")
//        return super.visitArray(name)
//    }
//}
//
//
//@Throws(Throwable::class)
//private fun InputStream.readClass(markAppAsmSource: () -> Unit) = use {
//    val classReader = ClassReader(this)
////    classReader.accept(
////        WinkAsmScanVisitor(classReader) { className,
////                                          moduleConfig,
////                                          providesList,
////                                          dependencyList
////            ->
////            if (moduleConfig != null) {
////                moduleConfigList.add(moduleConfig)
////            }
////            providerInfoMap.insert(className, providesList, dependencyList)
////        }, 0
////    )
//}
//
//internal class WinkAsmScanVisitor(
//    private val classReader: ClassReader,
//    private val onVisitEnd: OnScanFinish
//) : ClassVisitor(Opcodes.ASM5) {
//
//    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
//        WinkLog.vNoLimit("[visitAnnotation] desc=$desc, visible=$visible")
//        return super.visitAnnotation(desc, visible)
//    }
//}
//
//internal class ModuleConfig(
//    val application: String,
//    val priority: Int
//)
//internal typealias OnScanFinish = (
//    className: String,
//    moduleConfig: ModuleConfig?,
//    providesList: List<Provides>,
//    dependencyList: List<Provides>
//) -> Unit
//
//internal data class Provides(
//    val targetClass: String,
//    val targetName: String
//) {
//    val possibleTargetClasses: MutableSet<String> = mutableSetOf()
//}