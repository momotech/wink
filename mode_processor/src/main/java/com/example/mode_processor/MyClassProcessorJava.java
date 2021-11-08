package com.example.mode_processor;

import com.example.lib.Default;
import com.example.lib.MediaLayouter;
import com.example.lib.ModeJudger;
import com.example.lib.ModeJudgerAnd;
import com.example.lib.ModeJudgerOR;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyClassProcessorJava extends AbstractProcessor {

    private static final String BASE_ANCHOR_COMPONENT = "BaseAnchorComponent";
    private static final String BASE_AUDIENCE_COMPONENT = "BaseAudienceComponent";

    private static final String DOT = ".";
    private static final String COMPONENT_NAME = "mComponent";
    private static final String MODE_CREATOR_SUFFIX = "ModeCreator";
    private static final String GENERATE_PACKAGE_NAME = "com.immomo.molive.mode_template";
    private static final String BASE_AUDIENCE_MODE_JUDGER_PATH = "com.immomo.molive.connect.common.audience.BaseAudienceComponentModeJudger";
    private static final String BASE_ANCHOR_MODE_JUDGER_PATH = "com.immomo.molive.connect.common.anchor.BaseAnchorComponentModeJudger";
    private static final String AUDIENCE_MODE_JUDGER_LISTENER_PATH = "com.immomo.molive.connect.common.audience.AudienceModeJudgerEventListener";
    private static final String ANCHOR_MODE_JUDGER_LISTENER_PATH = "com.immomo.molive.connect.common.anchor.AnchorModeJudgerEventListener";
    private static final String BASE_MEDIA_LAYOUTER_PATH = "com.immomo.molive.gui.activities.live.medialayout.layouter.AbsMediaLayouter";
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotation = new HashSet<>();
        annotation.add(ModeJudger.class.getCanonicalName());
        annotation.add(ModeJudgerOR.class.getCanonicalName());
        annotation.add(ModeJudgerAnd.class.getCanonicalName());
        annotation.add(MediaLayouter.class.getCanonicalName());
        return annotation;
    }

    public void loggerInfo(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }
        loggerInfo("process start");
        StringBuilder printInfo = new StringBuilder();
//        Set<? extends Element> modeJudgerElements = roundEnvironment.getElementsAnnotatedWith(ModeJudger.class);
//        try {
//            if (modeJudgerElements != null && modeJudgerElements.size() > 0) {
//                printInfo.append(modeJudgerElements.size()).append("个文件加了@Route注解！");
//            }
//        } catch (Exception e) {
//            loggerInfo(e.getMessage());
//        }
//        List<String> classNames = new ArrayList<>();
//        for (Element routeElement : modeJudgerElements) {
//            loggerInfo("\nElement routeElement : routeElements ===========================>>>>>>>>>> 111111" + routeElement.getClass().getCanonicalName());
//            createModeJudger(printInfo, routeElement, false);
////            createModeController(printInfo, routeElement);
//            classNames.add(routeElement.getSimpleName().toString() + MODE_CREATOR_SUFFIX);
//        }
        createHelperFile(roundEnvironment, printInfo, null);
//        generateMediaLayouterList(roundEnvironment);
        return true;
    }

    private void generateMediaLayouterList(RoundEnvironment roundEnvironment) {
        try {
            List<String> classNames = new ArrayList<>();
            Set<? extends Element> mediaLayouterElement = roundEnvironment.getElementsAnnotatedWith(MediaLayouter.class);
            for (Element element : mediaLayouterElement) {
//                classNames.add(((Symbol.ClassSymbol) element).getQualifiedName().toString());
                classNames.add(getQualifiedName(element));
            }

            MethodSpec audienceMethodInit = MethodSpec.methodBuilder("getList")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeVariableName.get("List<" + BASE_MEDIA_LAYOUTER_PATH + ">"))
                    .addCode(createMediaLayouterFactoryCode(classNames))
                    .build();
            //构建类
            TypeSpec typeClass = TypeSpec.classBuilder("MediaLayouterGen")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(audienceMethodInit)
                    .build();
            //构建文件并指定生成文件目录
            JavaFile javaFile = JavaFile.builder(GENERATE_PACKAGE_NAME, typeClass).build();
            loggerInfo("process end");
            try {
                //把类、方法、参数等信息写入文件
                javaFile.writeTo(filer);
            } catch (IOException e) {
                loggerInfo("process exception");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createHelperFile(RoundEnvironment roundEnvironment, StringBuilder printInfo, List<String> classNames) {
        List<String> anchorClassNames = new ArrayList<>();
        List<String> audienceClassNames = new ArrayList<>();
        Set<? extends Element> modeJudgerGroupElement = null;

        try {
            modeJudgerGroupElement = roundEnvironment.getElementsAnnotatedWith(ModeJudgerAnd.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            modeJudgerGroupElement = roundEnvironment.getElementsAnnotatedWith(ModeJudgerOR.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Element routeElement : modeJudgerGroupElement) {
            loggerInfo("\nElement routeElement : routeElementGroups ===========================>>>>>>>>>>");
            try {
                String superClassName = getSimpleClassName(routeElement);
//                String superClassName = ((Symbol.ClassSymbol) routeElement).getSuperclass().tsym.getSimpleName().toString();
                //TODO-YWB
//                createModeJudger(printInfo, routeElement, true);
//                createModeController(printInfo, routeElement);
                if (isAnchor(superClassName)) {
                    anchorClassNames.add(routeElement.getSimpleName().toString() + MODE_CREATOR_SUFFIX);
                } else {
                    audienceClassNames.add(routeElement.getSimpleName().toString() + MODE_CREATOR_SUFFIX);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ParameterSpec param = ParameterSpec.builder(TypeVariableName.get(ANCHOR_MODE_JUDGER_LISTENER_PATH), "modeJudgerEventListener").build();
        MethodSpec anchorMethodInit = MethodSpec.methodBuilder("getAnchorModeList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(param)
                .returns(TypeVariableName.get("List<" + BASE_ANCHOR_MODE_JUDGER_PATH + ">"))
                .addCode(createFactoryCode(anchorClassNames, true))
                .build();

        ParameterSpec audienceParam = ParameterSpec.builder(TypeVariableName.get(AUDIENCE_MODE_JUDGER_LISTENER_PATH), "modeJudgerEventListener").build();
        MethodSpec audienceMethodInit = MethodSpec.methodBuilder("getAudienceModeList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(audienceParam)
                .returns(TypeVariableName.get("List<" + BASE_AUDIENCE_MODE_JUDGER_PATH + ">"))
                .addCode(createFactoryCode(audienceClassNames, false))
                .build();
        //构建类
        TypeSpec typeClass = TypeSpec.classBuilder("ModeJudgerHelper")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                //TODO-YWB
//                .addMethod(anchorMethodInit)
//                .addMethod(audienceMethodInit)
                .build();
        //构建文件并指定生成文件目录
        JavaFile javaFile = JavaFile.builder(GENERATE_PACKAGE_NAME, typeClass).build();
        loggerInfo("process end");
        try {
            //把类、方法、参数等信息写入文件
            javaFile.writeTo(filer);
        } catch (IOException e) {
            loggerInfo("process exception");
            e.printStackTrace();
        }
    }

    private boolean isAnchor(String superClassName) {
        return BASE_ANCHOR_COMPONENT.equals(superClassName);
    }

    private void createModeJudger(StringBuilder printInfo, Element routeElement, boolean multi) throws Exception {
        String className = routeElement.getSimpleName().toString();
        String fullPathClassName = getQualifiedName(routeElement);
        String superClassName = getSimpleClassName(routeElement);
//        String fullPathClassName = ((Symbol.ClassSymbol) routeElement).getQualifiedName().toString();
//        String superClassName = ((Symbol.ClassSymbol) routeElement).getSuperclass().tsym.getSimpleName().toString();

        //构建参数
        ParameterSpec msg = ParameterSpec.builder(String.class, "msg").build();
        //构建方法
        MethodSpec method = MethodSpec.methodBuilder("printLog")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(msg)
                .addStatement("$T.out.println($S + msg)", System.class, printInfo.toString())
                .build();

        String modeName = "";
        String connectModeName = "";
        CodeBlock.Builder liveJudgeCodeBuilder = CodeBlock.builder();
        int judgeSeiType = Default.SEI;
        try {
            if (multi) {
                try {
                    ModeJudgerAnd annotation = routeElement.getAnnotation(ModeJudgerAnd.class);
                    String code = CodeTemplate.getCode(routeElement, Type.TYPE_MODE_JUDGER_AND, messager);
                    if (code != null && !"".equals(code)) {
                        liveJudgeCodeBuilder.addStatement(code);
                        modeName = annotation.liveMode();
                        connectModeName = annotation.connectMode();
                    }
                    try {
                        judgeSeiType = annotation.judgeSei();
                    } catch (Exception ignore) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ModeJudgerOR annotation = routeElement.getAnnotation(ModeJudgerOR.class);
                    String code = CodeTemplate.getCode(routeElement, Type.TYPE_MODE_JUDGER_OR, messager);
                    if (code != null && !"".equals(code)) {
                        liveJudgeCodeBuilder.addStatement(code);
                        modeName = annotation.liveMode();
                        connectModeName = annotation.connectMode();
                    }
                    try {
                        judgeSeiType = annotation.judgeSei();
                    } catch (Exception ignore) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
//                loggerInfo("\n注解 modeTest ========================: " + routeElement.getAnnotation(ModeJudger.class).modeTest().getClass().getCanonicalName() + "." + routeElement.getAnnotation(ModeJudger.class).modeTest().name());
                liveJudgeCodeBuilder.addStatement(CodeTemplate.getCode(routeElement, Type.TYPE_MODE_JUDGER, messager));
                ModeJudger annotation = routeElement.getAnnotation(ModeJudger.class);
                modeName = annotation.liveMode();
                judgeSeiType = annotation.judgeSei();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CodeBlock liveJudgeCode = liveJudgeCodeBuilder.addStatement("    return false").build();

        MethodSpec methodJudge = MethodSpec.methodBuilder("judged")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(boolean.class)
                .addCode(liveJudgeCode)
                .build();

        CodeBlock liveModeReturn = CodeBlock.builder()
                .addStatement("return \"" + modeName + "\"")
                .build();

        MethodSpec methodGetLiveMode = MethodSpec.methodBuilder("getLiveMode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addJavadoc("使用 String 代替 ILiveActivity 解耦")
                .returns(String.class)
                .addCode(liveModeReturn)
                .build();

        CodeBlock connectModeReturn = CodeBlock.builder()
                .addStatement("return \"" + connectModeName + "\"")
                .build();

        MethodSpec methodGetConnectMode = MethodSpec.methodBuilder("getConnectMode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addJavadoc("使用 String 代替 ConnectMode 解耦")
                .returns(String.class)
                .addCode(connectModeReturn)
                .build();

        MethodSpec methodCreateComponent = MethodSpec.methodBuilder("createComponent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ClassName.bestGuess(fullPathClassName))
                .addCode(createComponentCode(fullPathClassName))
                .build();

        MethodSpec methodGetComponent = MethodSpec.methodBuilder("getComponent")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(fullPathClassName))
                .addCode(getComponentCode(fullPathClassName))
                .build();

        boolean isAnchor = isAnchor(superClassName);
        String judgerListenerPath = isAnchor ? ANCHOR_MODE_JUDGER_LISTENER_PATH : AUDIENCE_MODE_JUDGER_LISTENER_PATH;

        // 构造函数
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeVariableName.get(judgerListenerPath), "mListener")
                .addCode("super(mListener);")
                .build();

        String modeJudgerFullPath = isAnchor ? BASE_ANCHOR_MODE_JUDGER_PATH : BASE_AUDIENCE_MODE_JUDGER_PATH;
        //构建类
        TypeSpec.Builder baseAudienceModeJudger = TypeSpec.classBuilder(className + MODE_CREATOR_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.bestGuess(modeJudgerFullPath), TypeVariableName.get(fullPathClassName)))
                .addField(TypeVariableName.get(fullPathClassName), COMPONENT_NAME)
                .addMethod(constructor)
                .addMethod(method)
                .addMethod(methodJudge)
                .addMethod(methodGetLiveMode)
                .addMethod(methodCreateComponent)
                .addMethod(methodGetComponent);

        if (isAnchor) {
            baseAudienceModeJudger.addMethod(methodGetConnectMode);
        }

        // 添加 judge(sei) 方法，判断 sei
        if (judgeSeiType != Default.SEI) {
            ParameterSpec seiParam = ParameterSpec.builder(String.class, "sei").build();

            String judgeSeiCallbackCodeStr = CodeTemplate.getSeiCallbackCode(judgeSeiType);
            CodeBlock methodSeiCallCode = CodeBlock.builder()
                    .addStatement(judgeSeiCallbackCodeStr)
                    .build();
            MethodSpec methodSeiCallBack = MethodSpec.methodBuilder("seiCallBack")
                    .addParameter(seiParam)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addCode(methodSeiCallCode)
                    .build();
            baseAudienceModeJudger.addMethod(methodSeiCallBack);

            String judgeSeiCodeStr = CodeTemplate.getJudgeSeiCode(judgeSeiType);
            CodeBlock methodJudgeSeiCode = CodeBlock.builder()
                    .addStatement(judgeSeiCodeStr)
                    .build();

            MethodSpec methodJudgeSei = MethodSpec.methodBuilder("judge")
                    .addParameter(seiParam)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.BOOLEAN)
                    .addCode(methodJudgeSeiCode)
                    .build();
            baseAudienceModeJudger.addMethod(methodJudgeSei);
        }
        TypeSpec typeClass = baseAudienceModeJudger.build();
        //构建文件并指定生成文件目录
        JavaFile javaFile = JavaFile.builder(GENERATE_PACKAGE_NAME, typeClass).build();
        loggerInfo("process end");
        try {
            //把类、方法、参数等信息写入文件
            javaFile.writeTo(filer);
        } catch (IOException e) {
            loggerInfo("process exception");
            e.printStackTrace();
        }
    }

    public static CodeBlock createMediaLayouterFactoryCode(List<String> classNames) {
        CodeBlock.Builder builder = CodeBlock.builder();
        String baseModeJudgerPath = BASE_MEDIA_LAYOUTER_PATH;
        builder.addStatement("$T<" + baseModeJudgerPath + "> list = new $T<" + baseModeJudgerPath + ">()", List.class, ArrayList.class);
        for (String className : classNames) {
            String classFullPath = className;
            builder.addStatement("list.add(new " + classFullPath + "())");
        }
        builder.addStatement("return list");
        return builder.build();
    }

    public static CodeBlock createFactoryCode(List<String> classNames, boolean isAnchor) {
        CodeBlock.Builder builder = CodeBlock.builder();
        String baseModeJudgerPath = isAnchor ? BASE_ANCHOR_MODE_JUDGER_PATH : BASE_AUDIENCE_MODE_JUDGER_PATH;
        builder.addStatement("$T<" + baseModeJudgerPath + "> list = new $T<" + baseModeJudgerPath + ">()", List.class, ArrayList.class);
        for (String className : classNames) {
            String classFullPath = GENERATE_PACKAGE_NAME + DOT + className;
            builder.addStatement("list.add(new " + classFullPath + "(modeJudgerEventListener))");
        }
        builder.addStatement("return list");
        return builder.build();
    }

    public static CodeBlock getComponentCode(String fullPathClassName) {
        return CodeBlock.builder()
                .addStatement("return " + COMPONENT_NAME)
                .build();
    }

    public static CodeBlock createComponentCode(String fullPathClassName) {
        return CodeBlock.builder()
                .beginControlFlow("if (" + COMPONENT_NAME + " == null)")
                .addStatement(COMPONENT_NAME + " = new " + fullPathClassName + "()")
                .addStatement(COMPONENT_NAME + ".setModeJudger(mModeJudgerEventListener)")
                .endControlFlow()
                .addStatement("return " + COMPONENT_NAME)
                .build();
    }

    private String getSimpleClassName(Element routeElement) throws Exception {
        ClassLoader classLoader = routeElement.getClass().getClassLoader();
        Class cls = classForName("com.sun.tools.javac.code.Symbol$ClassSymbol", classLoader);
        Method m = cls.getDeclaredMethod("getSuperclass",new Class[]{});

//        System.out.println("ByteArrayOutputStream.getClass().getClassLoader() = " + cls.getClassLoader());
//        System.out.println("ByteArrayOutputStream.getClass().getClassLoader() = " + classLoader);

        Class clsType = classForName("com.sun.tools.javac.code.Type", classLoader);
        Field field = clsType.getDeclaredField("tsym");
        Object objType = field.get(m.invoke(routeElement));

        Class symbol = classForName("com.sun.tools.javac.code.Symbol", classLoader);
        Method m1 = symbol.getDeclaredMethod("getSimpleName",new Class[]{});
        Object objSimpleName = m1.invoke(field.get(m.invoke(routeElement)));

        Class nameClass = classForName("com.sun.tools.javac.util.Name", classLoader);
        Method toString = nameClass.getDeclaredMethod("toString",new Class[]{});

        String result = (String) toString.invoke(objSimpleName);
        System.out.println("result ----------------->>>: " + result);
        return result;
    }

    private String getQualifiedName(Element routeElement) throws Exception {
        ClassLoader classLoader = routeElement.getClass().getClassLoader();
        Class cls = classForName("com.sun.tools.javac.code.Symbol$ClassSymbol", classLoader);
        Method m = cls.getDeclaredMethod("getQualifiedName",new Class[]{});
        Object obj = m.invoke(routeElement);

        Class nameClass = classForName("com.sun.tools.javac.util.Name", classLoader);
        Method toString = nameClass.getDeclaredMethod("toString",new Class[]{});
        String result = (String) toString.invoke(obj);
        System.out.println("result : ==================>>>" + result);
        return result;
    }

    private Class classForName(String classPath, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(classPath,true, classLoader);
    }

}