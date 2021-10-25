package com.immomo.wink.helper;


import com.immomo.wink.Settings;
import com.immomo.wink.WinkOptions;
import com.immomo.wink.util.KaptEncodeUtils;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.text.StringsKt;

public class CompileHelper {

    public void compileCode() {
        File file = new File(Settings.env.tmpPath + "/tmp_class");
        if (!file.exists()) {
            file.mkdirs();
        }

        //变更注解的文件列表
        List<String> changedAnnotationList = getChangedAnnotationList();
//        List<String> changedAnnotationList = new ArrayList<>();
//        changedAnnotationList.add("/Users/momo/Documents/MomoProject/wink/wink-demo-app/src/main/java/com/immomo/wink/MainActivity3.java");
        if (changedAnnotationList.size() > 0) {
            changedAnnotationList.add(Settings.env.tmpPath + "/KaptCompileFile.kt");
        }
        WinkLog.d("changedAnnotationList >>>>>>>>>>>>>>>>>>> : " + changedAnnotationList.toString());


        compileKapt(changedAnnotationList);

        for (Settings.ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileKotlin(project);
        }

        for (Settings.ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileJava(project);
        }

        if (changedAnnotationList.size() > 0) {
            String classPathStr = getFullClasspathString();
            compileKaptFile(classPathStr);
        }
        createDexPatch();
    }

    @NotNull
    private String getFullClasspathString() {
        Set<String> hashSet = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Settings.data.projectBuildSortList.size(); i++) {
            if (TextUtils.isEmpty(Settings.data.projectBuildSortList.get(i).fixedInfo.classPath)) {
                continue;
            }
            String[] classPathArr = Settings.data.projectBuildSortList.get(i).fixedInfo.classPath.split(":");
            Collections.addAll(hashSet, classPathArr);
        }
        for (String classPath : hashSet) {
            sb.append(classPath);
            sb.append(":");
        }
        if (sb.lastIndexOf(":") == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 获取所有修改文件所影响的注解类
     */
    private List<String> getChangedAnnotationList() {
        List<String> annotationFiles = new ArrayList<>();
        if (Settings.data.processorMapping == null) {
            WinkLog.i("注解映射关系为空，本次无法编译注解");
            return annotationFiles;
        }
        WinkLog.d("Settings.data.processorMapping : " + Settings.data.processorMapping.toString());
        for (Settings.ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            // kotlin 记录的路径为 /Users/momo/Documents/MomoProject/wink/wink-demo-app/build/tmp/kapt3/stubs/debug/com/immomo/wink/MainActivity2.java
            for (String changedKotlinFile : project.changedKotlinFiles) {
                WinkLog.d("changedKotlinFile >>>>>>>>>>>>>>>>>>>>> " + changedKotlinFile);
                List<String> annotations = Settings.data.processorMapping.file2AnnotationsMapping.get(changedKotlinFile);
                if (annotations == null || annotations.size() == 0) {
                    continue;
                }
                for (String annotation : annotations) {
                    List<String> fileList = Settings.data.processorMapping.annotation2FilesMapping.get(annotation);
                    annotationFiles.addAll(fileList);
                }
            }
            for (String changedJavaFile : project.changedJavaFiles) {
                List<String> annotations = Settings.data.processorMapping.file2AnnotationsMapping.get(changedJavaFile);
                if (annotations == null || annotations.size() == 0) {
                    continue;
                }
                for (String annotation : annotations) {
                    List<String> fileList = Settings.data.processorMapping.annotation2FilesMapping.get(annotation);
                    annotationFiles.addAll(fileList);
                }
            }
        }
        return annotationFiles;
    }

    private int compileJava(Settings.ProjectTmpInfo project) {
        if (project.changedJavaFiles.size() <= 0) {
            return 0;
        }

        WinkLog.i("Compile " + project.changedJavaFiles.size() + " java files, module " + project.fixedInfo.name + ", " + project.changedJavaFiles.toString());
        StringBuilder sb = new StringBuilder();
        for (String path : project.changedJavaFiles) {
            sb.append(" ");
            sb.append(path);
        }

        String shellCommand = "javac" + project.fixedInfo.javacArgs
                + sb.toString();
        WinkLog.d("[LiteBuild] : javac shellCommand = " + shellCommand);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        Utils.runShells(
                shellCommand
        );

        Settings.data.classChangedCount += 1;

        return project.changedJavaFiles.size();
    }

    //TODO-YWB: classpath 去重
    private void compileKaptFile(String classPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(Settings.env.tmpPath).append("/tmp_class/com/alibaba/android/arouter/routes/*.java");
        WinkLog.d("[compileKaptFile]", sb.toString());
        if (Settings.env.kaptTaskParam != null && Settings.env.kaptTaskParam.processorOptions != null) {
            for (String processorOption : Settings.env.kaptTaskParam.processorOptions) {
                String[] split = processorOption.split(":");
                String[] split1 = split[split.length - 1].split("=");
                WinkLog.w("processorOption : " + split1[0] + " === " + split1[1]);
                if ("eventBusIndex".equals(split1[0])) {
                    sb.append(" ").append(Settings.env.tmpPath).append("/tmp_class/").append(split1[1].replace(".", "/")).append(".java");
                }
            }
        }

        StringBuilder commandPre = new StringBuilder();
        commandPre.append("javac");
        for (int i = 0; i <  Settings.env.javaCommandPre.size(); i++) {
            commandPre.append(" ");
            commandPre.append( Settings.env.javaCommandPre.get(i));
        }

        String command = commandPre.toString() + " " + classPath + " -d " + Settings.env.tmpPath + "/tmp_class" + sb.toString();
        Utils.runShells(command);
    }

    private void compileKapt(List<String> changedAnnotationList) {
        if (changedAnnotationList.size() <= 0) {
            WinkLog.d("LiteBuild: ================> 没有 annotation 文件变更。");
            return;
        }
        String kotlinc = getKotlinc();
        try {
            String javaHomePath = Settings.env.javaHome;
            javaHomePath = javaHomePath.replace(" ", "\\ ");
            Settings.KaptTaskParam kaptTaskParam = Settings.env.kaptTaskParam;

            Map<String, String> apoptionsMap = new HashMap<>();
            if (kaptTaskParam != null && kaptTaskParam.processorOptions != null) {
                for (String processorOption : kaptTaskParam.processorOptions) {
                    String[] split = processorOption.split(":");
                    String[] split1 = split[split.length - 1].split("=");
                    apoptionsMap.put(split1[0], split1[1]);
                }
            }

            if (Settings.env.annotationProcessorOptions != null) {
                Settings.env.annotationProcessorOptions.forEach(apoptionsMap::put);
            }

            StringBuilder changedAnnotationSb = new StringBuilder();
            for (String s : changedAnnotationList) {
                changedAnnotationSb.append(" ");
                changedAnnotationSb.append(s);
            }

            String shellCommand = "sh " + kotlinc + " \\\n"
                    + "-verbose \\\n"
                    + "-jdk-home " + javaHomePath + " \\\n"
                    + "-classpath " + Settings.env.kaptCompileClasspath + ":" + Settings.env.kaptProcessingClasspath + " \\\n"
                    + getKapt3Params(KaptEncodeUtils.encodeList(apoptionsMap)) + getApClasspath(Settings.env.kaptProcessingClasspath)
                    + getKotlinAnnotationProcessing()
                    + getJdkToolsPath()
                    + Settings.env.jvmTarget + " \\\n"
                    + changedAnnotationSb.toString();
            Utils.ShellResult result = Utils.runShells(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings.data.classChangedCount += 1;
    }


    private boolean hasAnnotationProcessors(File file) {
        String processorEntryPath = "META-INF/services/javax.annotation.processing.Processor";

        try {
//                file.isDirectory -> {
//                    return file.resolve(processorEntryPath).exists()
//                }
            if (file.isDirectory()) {
                File test = new File(file.getAbsolutePath() + File.separator + processorEntryPath);
                return test.exists();
            }

//                file.isFile && file.extension.equals("jar", ignoreCase = true) -> {
//                    return JarFile(file).use { jar ->
//                            jar.getJarEntry(processorEntryPath) != null
//                    }
//                }
            if (file.isFile() && file.getName().substring(file.getName().lastIndexOf(".")).equalsIgnoreCase(".jar")) {
                return (new JarFile(file).getJarEntry(processorEntryPath)) != null;


            }
        } catch (Exception e) {
            WinkLog.d("Could not check annotation processors existence in $file: $e");
        }
        return false;
    }

    private String getApClasspath(String processingClassPath) {
        if (TextUtils.isEmpty(processingClassPath)) {
            return "";
        }


        StringBuilder sb = new StringBuilder();
        String[] processingPath = processingClassPath.split(":");
        for (String s : processingPath) {
            if (KaptEncodeUtils.hasAnnotationProcessors(new File(s))) {
                sb.append("-P plugin:org.jetbrains.kotlin.kapt3:apclasspath=");
                sb.append(s);
                sb.append(" \\\n");
            }
//            hasAnnotationProcessors(new File(s));
        }
        return sb.toString();
    }


    private String getKotlinAnnotationProcessing() {
        String kotlincPath = getKotlinc();
        return "-Xplugin=" + kotlincPath.replace("bin/kotlinc", "lib/kotlin-annotation-processing.jar \\\n");
    }

    private String getJdkToolsPath() {
        String javaHomePath = Settings.env.javaHome;
        return "-Xplugin=" + javaHomePath.replace(" ", "\\ ").replace("Home/jre", "Home/lib/tools.jar \\\n");
    }


    private String getKapt3Params(String kaptEncodeOption) {
        return "-P plugin:org.jetbrains.kotlin.kapt3:sources=.idea/wink/tmp_class \\\n" +
                "-P plugin:org.jetbrains.kotlin.kapt3:classes=.idea/wink/tmp_class \\\n" +
                "-P plugin:org.jetbrains.kotlin.kapt3:stubs=.idea/wink/tmp_kapt_stubs \\\n" +
                "-P plugin:org.jetbrains.kotlin.kapt3:correctErrorTypes=true \\\n" +
                "-P plugin:org.jetbrains.kotlin.kapt3:aptMode=stubsAndApt \\\n" +
                "-P plugin:org.jetbrains.kotlin.kapt3:apoptions=" + kaptEncodeOption + " \\\n";
    }


    private void compileKotlin(Settings.ProjectTmpInfo project) {
        if (project.changedKotlinFiles.size() <= 0) {
            WinkLog.d("LiteBuild: ================> 没有 Kotlin 文件变更。");
            return;
        }

        WinkLog.i("Compile " + project.changedKotlinFiles.size() + " kotlin files, module " + project.fixedInfo.name + ", " + project.changedKotlinFiles.toString());
        StringBuilder sb = new StringBuilder();
        for (String path : project.changedKotlinFiles) {
            sb.append(" ");
            sb.append(path);
        }

        String kotlinHome = System.getenv("KOTLIN_HOME");
        if (kotlinHome == null || kotlinHome.equals("")) {
            kotlinHome = "/Applications/Android Studio.app/Contents/plugins/Kotlin";
        }

        String kotlinc = getKotlinc();

        WinkLog.d("[LiteBuild] kotlincHome : " + kotlinc);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        try {
            String mainKotlincArgs = project.fixedInfo.kotlincArgs;

            // todo apt
//            String kotlinxArgs = buildKotlinAndroidPluginCommand(kotlinHome, project);

            String javaHomePath = Settings.env.javaHome;
            javaHomePath = javaHomePath.replace(" ", "\\ ");

            // todo apt
//            String shellCommand = "sh " + kotlinc + kotlinxArgs + " -jdk-home " + javaHomePath
//                    + mainKotlincArgs + sb.toString();
            String shellCommand = "sh " + kotlinc + " -jdk-home " + javaHomePath
                    + mainKotlincArgs + sb.toString();

            WinkLog.d("[LiteBuild] kotlinc shellCommand : " + shellCommand);
            Utils.ShellResult result = Utils.runShells(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings.data.classChangedCount += 1;
    }

    private String getKotlinc() {
        String kotlinc = System.getenv("KOTLINC_HOME");
        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            kotlinc = "/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            kotlinc = "/Applications/AndroidStudio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            WinkLog.throwAssert("\n\n================== 请配置 KOTLINC_HOME =================="
                    + "\n1. 打开：~/.bash_profile"
                    + "\n2. 添加：export KOTLINC_HOME=\"/Applications/Android\\ Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc\""
                    + "\n3. 执行：source ~/.bash_profile"
                    + "\n========================================================\n\n");

            return "";
        }

        // 如果路径包含空格，需要替换 " " 为 "\ "
        if (!kotlinc.contains("\\")) {
            kotlinc = kotlinc.replace(" ", "\\ ");
        }

        return kotlinc;
    }

    private String buildKotlinAndroidPluginCommand(String kotlinHome, Settings.ProjectTmpInfo projectInfo) {
        WinkOptions options = Settings.env.options;
        String args = "";
        if (options.kotlinSyntheticsEnable) {
            String pluginHome = kotlinHome + "/kotlinc/lib/android-extensions-compiler.jar";
            String packageName = Settings.env.debugPackageName;
            String flavor = "main";
            String resPath = projectInfo.fixedInfo.dir + "/src/" + flavor + "/res";

            args = String.format(Locale.US, " -Xplugin=%s " +
                    "-P plugin:org.jetbrains.kotlin.android:package=%s " +
                    "-P plugin:org.jetbrains.kotlin.android:variant='%s;%s' ", pluginHome, packageName, flavor, resPath);
        }
        WinkLog.d("【compile kotlinx.android.synthetic】 \n" + args);
        return args;
    }

    private void findAptRelativeFiles(List<String> changeJavaFiles) {
        HashSet<String> fileSet = new HashSet<>();
    }

    private void createDexPatch() {
        if (Settings.data.classChangedCount <= 0) {
            // 没有数据变更
            return;
        }

        String patchName = Settings.env.version + "_patch.jar";
        String cmds = useD8(patchName);

        WinkLog.TimerLog log = WinkLog.timerStart("开始打DexPatch！");

        Utils.runShells(Utils.ShellOutput.NONE, cmds);

        log.end();
    }

    public String useD8(String patchName) {
        String cmds = "";
        String dest = Settings.env.tmpPath + "/tmp_class.zip";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "rm -rf " + dest;
        cmds += '\n' + "cd " + Settings.env.tmpPath + "/tmp_class";
        cmds += '\n' + "zip -r -o -q " + dest + " *";
        cmds += '\n' + Settings.env.buildToolsDir + "/d8 --intermediate --output " + Settings.env.tmpPath + "/" + patchName
                + " " + Settings.env.tmpPath + "/tmp_class.zip";

        if (Settings.data.hasResourceAddOrRename) {
            cmds += " " + Settings.env.appProjectDir + "/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/" + Settings.env.variantName + "/R.jar";
        }
        return cmds;
    }

    public String useDx(String patchName, String destPath) {
//        Utils.runShell("source ~/.bash_profile" +
//                '\n' + "adb shell mkdir " + destPath);

//        String classpath = " --classpath " + Settings.env.projectTreeRoot.classPath.replace(":", " --classpath ");
//        classpath = "";

//        WinkLog.v("Dex生成命令cmd =======\n" + cmds);
        String cmds = "";
        cmds += '\n' + Settings.env.buildToolsDir + "/dx --dex --no-strict --output "
                + Settings.env.tmpPath + "/" + patchName + " " + Settings.env.tmpPath + "/tmp_class/";

        cmds += '\n' + "adb shell mkdir " + destPath;
        cmds += '\n' + "adb push " + Settings.env.tmpPath + "/" + patchName + " " + destPath;
        return cmds;
    }
}