package com.immomo.wink;

import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencySpec;
import com.intellij.openapi.project.Project;

import java.io.File;

public class ConstantPool {
    public static String COMMAND = "./gradlew wink";

    public static String COMMAND_INIT = "./gradlew winkInit";

    public static String IDEA_WINK_DIR = ".idea/wink/";

    public static String IDEA_WINK_PLUGIN_DIR = IDEA_WINK_DIR+"/plugin/";

    public static String IDEA_WINK_LIB_DIR = ".idea/wink/lib/";

    public static String IEAD_DIR = IDEA_WINK_PLUGIN_DIR+"backup";
    public static String IEAD_WINK_JAR = IDEA_WINK_PLUGIN_DIR+"jars";
    public static String WINK_CONFIG = IDEA_WINK_PLUGIN_DIR+"wink.config";

    public static String LAST_INSTALL_MODULE = "last_install_module";
    public static String LAST_MAIN_PATH = "last_main_build";
    public static String LAST_ROOT_PATH = "last_root_build";
    public static String LAST_CHECK_DATA = "check_day";

    public static String MAIN_PROJECT_NAME = "app";


    public static String BUILD_GRADLE_FILE = "build.gradle";

    public static String PLUGIN_BASE = "com.immomo.wink";
    public static String PLUGIN_EXT = "plugin";
    public static String PLUGIN_NAME = PLUGIN_BASE+":"+PLUGIN_EXT;
    public static String PLUGIN_APP_NAME = PLUGIN_BASE+"."+PLUGIN_EXT;

    public static String PLUGIN_VERSION = "0.2.3i";

    public static String CLASSPATH_MARK= "classpath";

    public static String ANDROID_APP_MARK = "com.android.application";
    public static String PLUGINS_MARK_1 = "id 'com.android.application'";
    public static String PLUGINS_MARK_2 = "id \"com.android.application\"";

    public static String PLUGINS_MARK_3 = "apply plugin: 'com.android.application'";
    public static String PLUGINS_MARK_4 = "apply plugin: \"com.android.application\"";

    public static final int PLUGINS_TYPE_OLD = 0;
    public static final int PLUGINS_TYPE_NEW = 1;

    //主工程 build.gradle 路径:
    public static final String UI_WINK_MAIN_BUILD = "Main project build.gradle path:";
    public static final String UI_WINK_ROOT_BUILD = "Root project build.gradle path:";

    public static final String USER_LINK = "https://wink-build.github.io/site/";
    public static final String INSTALL_HELPER = "Installation guidelines";
    public static final String DOCUMENT = "Document: ";

    public static final String UPDATE_URL = "https://wink-build.github.io/docs/version.json";
    public static final String CURRENT_IDEA_VERSION = "1.0.0";
    public static final String IDEA_VERSION = "idea_version";
    public static final String P_VERSION = "plugin_version";

    public static final String JAR_MAIN = "litebuild-gradle-plugin.jar";
    public static final String[] JAR_NAMES = new String[]{"kotlin-csv-jvm.jar","kotlin-logging-1.7.9.jar","kotlin-stdlib.jar","kotlin-stdlib-common.jar",JAR_MAIN,"org.eclipse.jgit.jar","slf4j-api.jar"};
    public static String WINK_SHELL = "wink.sh";

    public static String COMMAND_SHELL = IDEA_WINK_LIB_DIR+WINK_SHELL;
    public static String[] getPluginMarks(){
        return new String[]{PLUGINS_MARK_1,PLUGINS_MARK_2,PLUGINS_MARK_3,PLUGINS_MARK_4};
    }


    public static String getPluginClassPath(){
        return "\t\t"+CLASSPATH_MARK+" \""+PLUGIN_NAME+":"+PLUGIN_VERSION+"\"";
    }


    public static String getPluginRegister(int type){
        return type==0?("apply plugin: '"+PLUGIN_APP_NAME+"'"):("\tid '"+PLUGIN_APP_NAME+"'");
    }

    public static String getLinkAddress(){
        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append(DOCUMENT).append("<a href=\"\">").append(USER_LINK).append("</a></html>");
        return sb.toString();
    }

    public static ArtifactDependencySpec pluginClassPathDependence(){
        return ArtifactDependencySpec.create(PLUGIN_EXT,PLUGIN_BASE,PLUGIN_VERSION);
    }

    public static String getJarCommand(Project project){
        String projectPath = project.getBasePath();
        String jarBasePath = project.getBasePath()+"/"+ConstantPool.IEAD_WINK_JAR;
        StringBuilder sb = new StringBuilder();
        sb.append("java -Xbootclasspath/a:");

        for(int i=0;i<JAR_NAMES.length;i++){
            if(JAR_NAMES[i].equals(JAR_MAIN)){
                continue;
            }
            sb.append(jarBasePath+"/"+JAR_NAMES[i]);
            if(i==JAR_NAMES.length-1){
                sb.append(" -jar ");
            }else {
                sb.append(":");
            }
        }
        String runJarPath = jarBasePath+"/"+JAR_MAIN;
        sb.append(runJarPath).append(" ").append(projectPath);
        return sb.toString();
    }

}
