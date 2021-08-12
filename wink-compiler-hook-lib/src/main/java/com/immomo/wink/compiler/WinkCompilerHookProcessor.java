package com.immomo.wink.compiler;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("=======================");
        System.out.println("   process run !!!   ");
        System.out.println("=======================");
        try {
//            File file = new File("../.idea/wink/annotation/whitelist");
//            if (!file.exists()) {
//                return false;
//            }
//
//            String annotationWhiteListStr = null;
//            try {
//                BufferedReader input = new BufferedReader(new FileReader(file));
//                annotationWhiteListStr = input.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (annotationWhiteListStr == null
//                    || annotationWhiteListStr.isEmpty()) {
//                return false;
//            }
//
//            String[] whiteList = annotationWhiteListStr.split(",");

//            set.iterator().next().getQualifiedName()

            String[] whiteList = new String[]{"com.alibaba.android.arouter.facade.annotation.Route", "butterknife.BindView"};

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

                        if (element instanceof Symbol.ClassSymbol) {
                            symbol = (Symbol.ClassSymbol) element;
                        }

                        if (symbol == null) {
                            continue;
                        }

                        Field fileField = symbol.sourcefile.getClass().getDeclaredField("file");
                        fileField.setAccessible(true);
                        File sourceFile = (File) fileField.get(symbol.sourcefile);

                        if (sourceFile.exists()) {
                            if (!processorMapping.annotation2FilesMapping
                                    .containsKey(annotation)) {
                                processorMapping.annotation2FilesMapping.put(annotation, new ArrayList<String>());
                            }
                            processorMapping.annotation2FilesMapping.get(annotation).add(sourceFile.getAbsolutePath());

                            if (!processorMapping.file2AnnotationsMapping
                                    .containsKey(sourceFile.getAbsolutePath())) {
                                processorMapping.file2AnnotationsMapping.put(sourceFile.getAbsolutePath(), new ArrayList<String>());
                            }
                            processorMapping.file2AnnotationsMapping.get(sourceFile.getAbsolutePath()).add(annotation);
                        }
                    }
                    // 写入文件
                } catch (Exception e) {

                }
            }

            if (processorMapping.annotation2FilesMapping.size() > 0) {
                String userDirectory = new File("").getAbsolutePath();
                System.out.println("userDirectory ===>>>>>>>>>> " + userDirectory);

                File fileDir = new File(userDirectory + "/.idea/wink/annotation/");
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }

                System.out.println("processorMapping ===>>>>>>>>>>");
                System.out.println(processorMapping.toString());
                LocalCacheUtil.save2File(processorMapping, userDirectory + "/.idea/wink/annotation/mapping");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }
}