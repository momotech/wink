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

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.utils.FileUtils;
import com.immomo.wink.Settings;
import com.immomo.wink.WinkOptions;
import com.immomo.wink.util.AndroidManifestUtils;
import com.immomo.wink.util.LocalCacheUtil;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.WinkLog;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class InitEnvHelper {
    Project project;

    public void initEnv(Project project, boolean reload) {
        if (reload) {
            createEnv(project);
        } else {
            WinkLog.d("project.getRootDir() : " + project.getRootDir());
            Settings.restoreEnv(project.getRootDir()
                    + "/.idea/" + Settings.NAME + "/env");
        }

        initData(project);
    }

    public void initEnvFromCache(String rootPath) {
        Settings.restoreEnv(rootPath
                + "/.idea/" + Settings.NAME + "/env");

        // Data每次初始化
        Settings.initData();
    }

    public void createEnv(Project project) {
        this.project = project;

        AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");

        Settings.Env env = Settings.env;
        env.javaHome = getJavaHome();
        env.sdkDir = androidExt.getSdkDirectory().getPath();
        env.buildToolsVersion = androidExt.getBuildToolsVersion();
        env.buildToolsDir = FileUtils.join(androidExt.getSdkDirectory().getPath(),
                "build-tools", env.buildToolsVersion);
        env.compileSdkVersion = androidExt.getCompileSdkVersion();
        env.compileSdkDir = FileUtils.join(env.sdkDir, "platforms", env.compileSdkVersion);

        env.rootDir = project.getRootDir().getAbsolutePath();
        if (androidExt.getProductFlavors() != null && androidExt.getProductFlavors().getNames().size() > 0) {
            env.defaultFlavor = androidExt.getProductFlavors().getNames().first();
            env.variantName = env.defaultFlavor + "Debug";
        }

        if (!Settings.data.newVersion.isEmpty()) {
            env.version = Settings.data.newVersion;
            Settings.data.newVersion = "";
        }

        try {
            Repository rep = new RepositoryBuilder()
                    .findGitDir(new File(env.rootDir))
                    .build();
            env.branch = rep.getBranch();
            WinkLog.d("[IniEnvHelper] current branch:" + env.branch);
        } catch (Exception e) {
            e.printStackTrace();
        }


        env.appProjectDir = project.getProjectDir().getAbsolutePath();
        env.tmpPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME;

        env.packageName = androidExt.getDefaultConfig().getApplicationId();
        Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
        while (itApp.hasNext()) {
            ApplicationVariant variant = itApp.next();
            if (variant.getName().equals(env.variantName)) {
                env.debugPackageName = variant.getApplicationId();
                break;
            }
        }

        String manifestPath = androidExt.getSourceSets().getByName("main").getManifest().getSrcFile().getPath();
        env.launcherActivity = AndroidManifestUtils.findLauncherActivity(manifestPath, env.packageName);

        WinkOptions options = project.getExtensions().getByType(WinkOptions.class);
        env.options = options.copy();

        // todo apt
//        initKaptTaskParams(env);

        findModuleTree2(project, "");

        Settings.storeEnv(env, project.getRootDir() + "/.idea/" + Settings.NAME + "/env");
    }

    private void initData(Project project) {
        // Data每次初始化
        Settings.initData();

        initLog(project);
    }

    public void initLog(Project project) {
        if (project == null) {
            Settings.data.logLevel = Settings.env.options.logLevel;
        } else {
            WinkOptions options = project.getExtensions().getByType(WinkOptions.class);
            Settings.data.logLevel = options.logLevel;
        }

    }

    public void initE() {
        File fileDir = new File(Settings.env.tmpPath + "/annotation");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        StringBuilder sb = new StringBuilder();
        String[] injects = new String[] { "com.alibaba.android.arouter.facade.annotation.Route" };
        for (String inject : injects) {
            sb.append(inject);
            sb.append(",");
        }

        LocalCacheUtil.save2File(sb.toString(), Settings.env.tmpPath + "/annotation/whitelist");
    }

    public boolean isEnvExist(String path) {
        String envFilePath = path + "/.idea/" + Settings.NAME + "/env";
        File envFile = new File(envFilePath);
        return envFile.exists();
    }

    public boolean isBranchOK() {
        WinkLog.i("[IniEnvHelper] [isBranchOK]...");
        if (Utils.isEmpty(Settings.env.rootDir)) {
            return false;
        }
        try {
            Repository rep = new RepositoryBuilder()
                    .findGitDir(new File(Settings.env.rootDir))
                    .build();
//            Git git = new Git(rep);
            String curBranchName = rep.getBranch();
            WinkLog.d("[IniEnvHelper] curBranch=" + curBranchName +
                    ", env.branch=" + Settings.env.branch);
            if (!Settings.env.branch.equals(curBranchName)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // from retrolambda
    public String getJavaHome() {
        String javaHomeProp = System.getProperty("java.home");
        if (javaHomeProp != null && !javaHomeProp.equals("")) {
            int jreIndex = javaHomeProp.lastIndexOf("${File.separator}jre");
            if (jreIndex != -1) {
                return javaHomeProp.substring(0, jreIndex);
            } else {
                List<String> rets = new ArrayList<>();
                rets.toArray();
                Set<Integer> has = new HashSet<>();

                String str = "";
                new String(str.toCharArray());

                return javaHomeProp;
            }
        } else {
            return System.getenv("JAVA_HOME");
        }
    }

    public static String obtainAppDebugPackageName(Project project) {
        AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
        Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
        String variantName = "debug";
        String debugPackageName = "";
        WinkLog.d("[obtainAppDebugPackageName] start...");
        if (androidExt.getProductFlavors() != null && androidExt.getProductFlavors().getNames().size() > 0) {
            String defaultFlavor = androidExt.getProductFlavors().getNames().first();
            variantName = defaultFlavor + "Debug";
            WinkLog.d("[obtainAppDebugPackageName] variantName=" + variantName);
        }
        while (itApp.hasNext()) {
            ApplicationVariant variant = itApp.next();
            if (variant.getName().equals(variantName)) {
                debugPackageName = variant.getApplicationId();
                WinkLog.d("[obtainAppDebugPackageName] debugPackageName=" + debugPackageName);
                break;
            }
        }
        return debugPackageName;
    }

//    private void initKaptTaskParams(Settings.Env env) {
//        Settings.KaptTaskParam param = new Settings.KaptTaskParam();
//        Task kaptDebug = project.getTasks().getByName("kaptDebugKotlin");
//
//        if (kaptDebug instanceof KaptWithoutKotlincTask) {
////            Object object = project.getConfigurations().getByName("kotlinCompilerClasspath").resolve();
//            KaptWithoutKotlincTask ktask = (KaptWithoutKotlincTask) kaptDebug;
//            param.compileClassPath = ktask.getClasspath().getAsPath();
//            param.javacOptions = ktask.getJavacOptions();
//            param.javaSourceRoots = null;
////            param.processorOptions = ktask.processorOptions.getArguments();
//            try {
//                Field processorOptions = ShareReflectUtil.findField(ktask, "processorOptions");
//                CompilerPluginOptions options = (CompilerPluginOptions) processorOptions.get(ktask);
//                param.processorOptions = options.getArguments();
//
//                Field configurationContainer = ShareReflectUtil.findField(((DefaultProject) project), "configurationContainer");
//                ConfigurationContainer conf = (ConfigurationContainer) configurationContainer.get(((DefaultProject) project));
//                if (conf != null) {
//                    param.processingClassPath = conf.getByName("kapt").resolve();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        env.kaptTaskParam = param;
//    }

    private void initProjectData(Settings.ProjectFixedInfo fixedInfo, Project project) {
        initProjectData(fixedInfo, project, false);
    }

    private void initProjectData(Settings.ProjectFixedInfo fixedInfo, Project project, boolean foreInit) {
        long findModuleEndTime = System.currentTimeMillis();
        fixedInfo.name = project.getName();
        fixedInfo.isProjectIgnore = isIgnoreProject(fixedInfo.name);
        if (fixedInfo.isProjectIgnore && !foreInit) {
            return;
        }

        fixedInfo.dir = project.getProjectDir().getAbsolutePath();
        fixedInfo.buildDir = project.getBuildDir().getPath();

        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> kotlinArgs = new ArrayList<>();

        WinkLog.d("ywb 2222222 initProjectData 1111 耗时：" + (System.currentTimeMillis() - findModuleEndTime) + " ms");

        Object extension = project.getExtensions().findByName("android");
        JavaCompile javaCompile = null;
//        String processorArgs = "";
        if (extension == null) {
            return;
        } else if (extension instanceof AppExtension) {
//            processorArgs = getProcessorArgs(((AppExtension) extension).getDefaultConfig()
//                    .getJavaCompileOptions().getAnnotationProcessorOptions().getArguments());

            Iterator<ApplicationVariant> itApp = ((AppExtension) extension).getApplicationVariants().iterator();
            while (itApp.hasNext()) {
                ApplicationVariant variant = itApp.next();
                if (variant.getName().equals(Settings.env.variantName)) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        } else if (extension instanceof LibraryExtension) {
//            processorArgs = getProcessorArgs(((LibraryExtension) extension).getDefaultConfig()
//                    .getJavaCompileOptions().getAnnotationProcessorOptions().getArguments());

            Iterator<LibraryVariant> it = ((LibraryExtension) extension).getLibraryVariants().iterator();
            while (it.hasNext()) {
                LibraryVariant variant = it.next();
                if (variant.getName().equals(Settings.env.variantName)) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        }

        if (javaCompile == null) {
            return;
        }

        args.add("-source");
        args.add(javaCompile.getTargetCompatibility());

        args.add("-target");
        args.add(javaCompile.getTargetCompatibility());

        args.add("-encoding");
        args.add(javaCompile.getOptions().getEncoding());

        args.add("-bootclasspath");
        args.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath());

        args.add("-g");

//            args.add("-sourcepath");
//            args.add("");

        String processorpath = javaCompile.getOptions().getAnnotationProcessorPath().getAsPath();
        // todo apt
//        if (Settings.env.kaptTaskParam != null &&
//                Settings.env.kaptTaskParam.processorOptions != null) {
//            for (File file : Settings.env.kaptTaskParam.processingClassPath) {
//                if (processorpath != null && !processorpath.isEmpty()) {
//                    processorpath += ":";
//                }
//
//                processorpath += file.getAbsolutePath();
//            }
//        }

        // todo apt
//        if (!processorpath.trim().isEmpty()) {
//            args.add("-processorpath");
//            args.add(processorpath);
//        }
//
//        //注解处理器参数
//        if (extension instanceof BaseExtension) {
//            StringBuilder aptOptions = new StringBuilder();
//            AnnotationProcessorOptions annotationProcessorOptions = ((BaseExtension) extension).getDefaultConfig().getJavaCompileOptions().getAnnotationProcessorOptions();
//            annotationProcessorOptions.getArguments().forEach((k, v) -> aptOptions.append(String.format(Locale.US, "-A%s=%s ",k, v)));
//
//            // todo apt
////            // add kapt args
////            if (Settings.env.kaptTaskParam != null && Settings.env.kaptTaskParam.processorOptions != null) {
////                Settings.env.kaptTaskParam.processorOptions.forEach((v) -> aptOptions.append(String.format(Locale.US, "-A%s ", v)));
////            }
//
//            args.add(aptOptions.toString());
//        }

        args.add("-classpath");

        fixedInfo.classPath = javaCompile.getClasspath().getAsPath() + ":"
                + project.getProjectDir().toString() + "/build/intermediates/javac/" + Settings.env.variantName + "/classes"
                + ":" + Settings.env.tmpPath + "/tmp_class"
                + ":" + project.getProjectDir().toString() + "/build/generated/not_namespaced_r_class_sources/" + Settings.env.variantName + "/r";

        args.add(fixedInfo.classPath);

        args.add("-d");
        args.add(Settings.env.tmpPath+ "/tmp_class");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            sb.append(" ");
            sb.append(args.get(i));
        }

        fixedInfo.javacArgs = sb.toString();

        // 路径可能包含空格，动态控制
//            kotlinArgs.add("-jdk-home");
//            kotlinArgs.add(getJavaHome());

        kotlinArgs.add("-classpath");
//            WinkLog.d("=============");
//            WinkLog.d("BootstrapClasspath =========== : " + javaCompile.getOptions().getBootstrapClasspath().getAsPath());
//            WinkLog.d("=============");

        WinkLog.d("projectDir : " + project.getProjectDir().toString());
        kotlinArgs.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath() + ":"
                + fixedInfo.classPath);

//        if (processorArgs != null && !processorArgs.isEmpty()) {
//            kotlinArgs.add(processorArgs);
//        }

        kotlinArgs.add("-jvm-target");
        kotlinArgs.add(getSupportVersion(javaCompile.getTargetCompatibility()));

        kotlinArgs.add("-d");
        kotlinArgs.add(Settings.env.tmpPath + "/tmp_class");

        StringBuilder sbKotlin = new StringBuilder();
        for (int i = 0; i < kotlinArgs.size(); i++) {
            sbKotlin.append(" ");
            sbKotlin.append(kotlinArgs.get(i));
        }

        fixedInfo.kotlincArgs = sbKotlin.toString();
    }

    private String getProcessorArgs(Map<String, String> argsA) {
        StringBuilder sb = new StringBuilder();
        if (argsA.size() > 0) {
            boolean firstA = true;
            for (String key: argsA.keySet()) {
                if (!firstA) {
                    sb.append(".");
                }

                if (argsA.get(key) == null || argsA.get(key).isEmpty()) {
                    sb.append("-A" + key);
                } else {
                    sb.append("-A" + key + "=" + argsA.get(key));
                }
            }
        }

        return sb.toString();
    }

    private String getSupportVersion(String jvmVersion) {
        if ("1.7".equals(jvmVersion)) {
            return "1.8";
        }

        return jvmVersion;
    }

    private void findModuleTree(Project project, String productFlavor) {
        Settings.env.projectTreeRoot = new Settings.ProjectFixedInfo();

        HashSet<String> hasAddProject = new HashSet<>();
        hasAddProject.add(project.getName());

        handleAndroidProject(project, Settings.env.projectTreeRoot, hasAddProject, productFlavor, "debug");

        sortBuildList(Settings.env.projectTreeRoot, Settings.env.projectBuildSortList);
    }

    public void findModuleTree2(Project project, String productFlavor) {
        Settings.env.projectTreeRoot = new Settings.ProjectFixedInfo();
        initProjectData(Settings.env.projectTreeRoot, project, true);

        HashSet<String> hasAddProject = new HashSet<>();
        hasAddProject.add(project.getName());

        for (Project item : project.getRootProject().getSubprojects()) {
            String name = item.getName();
            if (name.equals("wink-gradle-plugin")
                    || name.equals("wink-patch-lib")
                    || hasAddProject.contains(name)) {
                continue;
            }

            Settings.ProjectFixedInfo childNode = new Settings.ProjectFixedInfo();
            initProjectData(childNode, item, false);
            Settings.env.projectTreeRoot.children.add(childNode);
            hasAddProject.add(item.getName());
        }

        Settings.env.projectBuildSortList.clear();
        sortBuildList(Settings.env.projectTreeRoot, Settings.env.projectBuildSortList);
    }

    private boolean isValidProject() {
        return false;
    }

    private boolean isIgnoreProject(String moduleName) {
        WinkOptions winkOptions = Settings.env.options;
        if (winkOptions == null) {
            return false;
        }

        if (winkOptions.moduleWhitelist != null
                && winkOptions.moduleWhitelist.length > 0) {
            for (String module : winkOptions.moduleWhitelist) {
                if (moduleName.equals(module)) {
                    return false;

                }
            }

            return true;
        } else if (winkOptions.moduleBlacklist != null
                && winkOptions.moduleBlacklist.length > 0) {
            for (String module : winkOptions.moduleBlacklist) {
                if (moduleName.equals(module)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    private void sortBuildList(Settings.ProjectFixedInfo node, List<Settings.ProjectFixedInfo> out) {
        for (Settings.ProjectFixedInfo child : node.children) {
            sortBuildList(child, out);
        }

        if (!node.isProjectIgnore) {
            out.add(node);
        }
    }

    private void handleAndroidProject(Project project, Settings.ProjectFixedInfo node,
                                      HashSet<String> hasAddProject, String productFlavor, String buildType) {
        initProjectData(node, project);

        String[] compileNames = new String[] {"compile", "implementation", "api", "debugCompile"};
        for (String name : compileNames) {
            Configuration compile = project.getConfigurations().findByName(name);
            if (compile != null) {
                collectLocalDependency(node, compile, hasAddProject, productFlavor, buildType);
            }
        }
    }

    private void collectLocalDependency(Settings.ProjectFixedInfo node,
                                        Configuration xxxCompile,
                                        HashSet<String> hasAddProject, String productFlavor, String buildType) {
        xxxCompile.getDependencies().forEach(new Consumer<Dependency>() {
            @Override
            public void accept(Dependency dependency) {

                if (dependency instanceof DefaultProjectDependency) {
                    DefaultProjectDependency dp = (DefaultProjectDependency) dependency;
                    // 孩子节点
                    String name = dp.getDependencyProject().getName();
                    if (name.equals("wink-gradle-plugin")
                            || name.equals("wink-patch-lib")
                            || hasAddProject.contains(name)) {
                        return;
                    }

                    Settings.ProjectFixedInfo childNode = new Settings.ProjectFixedInfo();
                    node.children.add(childNode);
                    hasAddProject.add(name);

                    handleAndroidProject(dp.getDependencyProject(), childNode,  hasAddProject, productFlavor, buildType);
                }
            }
        });
    }
}
