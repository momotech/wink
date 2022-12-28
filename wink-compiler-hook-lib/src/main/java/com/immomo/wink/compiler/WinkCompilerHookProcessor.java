package com.immomo.wink.compiler;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"*"})
public class WinkCompilerHookProcessor extends AbstractProcessor {

//    private Trees trees;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("=======================");
        System.out.println("   [Wink] processor run !!!   ");
        System.out.println("=======================");
//
//        System.out.println("TypeElement ===>>> : " + set.toString());

        /*
            出错情况：直接执行脚本
                ~~~~~~~~~~~~~~~~~~~~~~~~ getClass().getClassLoader(): java.net.URLClassLoader@791900c1
                ~~~~~~~~~~~~~~~~~~~~~~~~ Trees.class.getClassLoader(): java.net.URLClassLoader@791900c1
                ~~~~~~~~~~~~~~~~~~~~~~~~ processingEnv.getClass().getClassLoader() 1: java.net.URLClassLoader@63e31ee
            正常情况：命令行执行
                ~~~~~~~~~~~~~~~~~~~~~~~~ getClass().getClassLoader(): java.net.URLClassLoader@7edae3a
                ~~~~~~~~~~~~~~~~~~~~~~~~ Trees.class.getClassLoader(): sun.misc.Launcher$ExtClassLoader@5f2050f6
                ~~~~~~~~~~~~~~~~~~~~~~~~ processingEnv.getClass().getClassLoader() 1: sun.misc.Launcher$ExtClassLoader@5f2050f6

            This can be validated by running the jvm with -verbose:class:
            ...
            [Loaded com.sun.source.util.Trees from file:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/tools.jar]
            [Loaded com.sun.source.util.Trees from file:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/lib/tools.jar]
            ...
            You can see [com.sun.source.util.Trees] shows up twice, which confirms our theory.
         */

        // https://youtrack.jetbrains.com/issue/KT-24540
        // https://github.com/JetBrains/kotlin/pull/1772 [Kapt] Pass current ClassLoader as parent class loader by default
        // java.lang.AssertionError: java.lang.ClassCastException: com.sun.tools.javac.api.JavacTrees cannot be cast to com.sun.source.util.Trees
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ getClass().getClassLoader(): "
//                + getClass().getClassLoader().toString());
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ Trees.class.getClassLoader(): "
//                + Trees.class.getClassLoader().toString());
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ processingEnv.getClass().getClassLoader() 111: "
//                + processingEnv.getClass().getClassLoader().toString());

        // 此处模拟 ButterKnife 注解处理器出现异常时情况
        /*
        try {
            trees = Trees.instance(processingEnv);
//            System.out.println("Trees ===>>> : 1111111111111");
        } catch (IllegalArgumentException ignored) {
            System.out.println("Trees ===>>> : 22222222222222222");
            System.out.println("Trees ===>>> : " + ignored.toString());
            try {
                // Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one.
                for (Field field : processingEnv.getClass().getDeclaredFields()) {
                    if (field.getName().equals("delegate") || field.getName().equals("processingEnv")) {
                        field.setAccessible(true);
                        ProcessingEnvironment javacEnv = (ProcessingEnvironment) field.get(processingEnv);
                        trees = Trees.instance(javacEnv);
                        break;
                    }
                }
            } catch (Throwable ignored2) {
                System.out.println("Trees ===>>> : " + ignored2.toString());
                System.out.println("Trees ===>>> : 33333333333");
            }
        }
        */

        try {
            File file = new File(".idea/wink_annotation_whitelist.txt");
            System.out.println("file.getAbsolutePath() : " + file.getAbsolutePath());
            if (!file.exists()) {
                Log.i("wink_annotation_whitelist.txt file not exist");
                return false;
            }

            List<String> whiteList = new ArrayList<>();
            //BufferedReader是可以按行读取文件
            FileInputStream inputStream = new FileInputStream(".idea/wink_annotation_whitelist.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String str = null;
            while((str = bufferedReader.readLine()) != null)
            {
                whiteList.add(str);
//                System.out.println(str);
            }

            //close
            inputStream.close();
            bufferedReader.close();

            if (whiteList.size() == 0) {
                Log.i("===>>> wink_annotation_whitelist.txt is empty");
                return false;
            }

            Log.i("===>>> wink annotation list : " + whiteList.toString());

//            String[] whiteList = new String[]{
//                    "com.alibaba.android.arouter.facade.annotation.Route",
//                    "butterknife.BindView"
//            };

//            String annotation = "com.alibaba.android.arouter.facade.annotation.Route";
            ProcessorMapping processorMapping = new ProcessorMapping();
            for (String annotation: whiteList) {
                try {
                    Set<? extends Element> elements = roundEnvironment
                            .getElementsAnnotatedWith((Class<? extends Annotation>) Class.forName(annotation));
                    for (Element element : elements) {
                        Symbol.ClassSymbol symbol = null;
                        if (element instanceof Symbol.VarSymbol) {
                            symbol = (Symbol.ClassSymbol) ((Symbol.VarSymbol) element).owner;
                        }
//                        element.getAnnotation(Metadata.class);
                        if (element instanceof Symbol.ClassSymbol) {
                            symbol = (Symbol.ClassSymbol) element;
                        }

                        if (symbol == null) {
                            continue;
                        }
                        // 文件地址
//                        symbol.sourcefile.getName();

                        String sourceFilePath = "";
                        File sourceFile = null;
                        try {
                            Field fileField = symbol.sourcefile.getClass().getDeclaredField("file");
                            fileField.setAccessible(true);
                            sourceFile = (File) fileField.get(symbol.sourcefile);
                            sourceFilePath = sourceFile.getAbsolutePath();
                        } catch (Exception e) {
                            sourceFilePath = symbol.sourcefile.getName();
                            sourceFile = new File(sourceFilePath);

                        }

                        /*
                         * 目前拿到的文件是 kapt 生成 stubs 的 .java 文件，没有好的方式直接获取到 .kt 文件
                         * /Users/momo/Documents/MomoProject/wink/wink-demo-app/build/tmp/kapt3/stubs/debug/com/immomo/wink/MainActivity2.java
                         * /Users/momo/Documents/MomoProject/wink/wink-demo-app/   src/main/java                 /com/immomo/wink/MainActivity3.java
                         * /Users/momo/Documents/MomoProject/wink/wink-demo-app/   build/tmp/kapt3/stubs/debug   /com/immomo/wink/MainActivity2.java
                         */

                        if (sourceFilePath.contains("build/tmp/kapt3/stubs/debug")) {
                            sourceFilePath = sourceFilePath.replace("build/tmp/kapt3/stubs/debug", "src/main/java").replace(".java", ".kt");
                        }

                        if (sourceFile.exists()) {
                            if (!processorMapping.annotation2FilesMapping
                                    .containsKey(annotation)) {
                                processorMapping.annotation2FilesMapping.put(annotation, new ArrayList<String>());
                            }
                            processorMapping.annotation2FilesMapping.get(annotation).add(sourceFilePath);

                            if (!processorMapping.file2AnnotationsMapping
                                    .containsKey(sourceFilePath)) {
                                processorMapping.file2AnnotationsMapping.put(sourceFilePath, new ArrayList<String>());
                            }
                            processorMapping.file2AnnotationsMapping.get(sourceFilePath).add(annotation);
                        }
                    }
                    // 写入文件
                } catch (Exception e) {

                }
            }

            System.out.println("processorMapping ===================================>>>>>>>>>>");
            if (processorMapping.annotation2FilesMapping.size() > 0) {
                String userDirectory = new File("").getAbsolutePath();
//                String userDirectory = "/Users/momo/Documents/MomoProject/wink";
                System.out.println("userDirectory ===>>>>>>>>>> " + userDirectory);

                File fileDir = new File(userDirectory + "/.idea/wink/annotation/");
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }

                System.out.println("processorMapping ===>>>>>>>>>>");
                System.out.println(processorMapping.toString());
                String filePath = userDirectory + "/.idea/wink/annotation/mapping";
                System.out.println("filePath : " + filePath);
                LocalCacheUtil.save2File(processorMapping, userDirectory + "/.idea/wink/annotation/mapping");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }
}