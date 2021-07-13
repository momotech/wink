package com.immomo.wink.utils;


import com.immomo.wink.ConstantPool;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.runtime.ArrayUtil;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.*;

public class FileWinkUtils {

    public static File[] findMathFile(File rootFile, String fileFilter) {
        File[] matchingFiles = rootFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(fileFilter) && name.endsWith(fileFilter);
            }
        });
        return matchingFiles;
    }

    public static ArrayList<File> findRootProjectBuild(File rootFile) {
        File[] matchingFiles = rootFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return dir.isDirectory();
            }
        });
        ArrayList<File> files = new ArrayList<>();
        for (File project : matchingFiles) {
            File[] findfiles = findMathFile(project, ConstantPool.BUILD_GRADLE_FILE);
            if (findfiles != null) {
                files.addAll(Arrays.asList(findfiles));
            }
        }

        return files;
    }


    private static boolean rootInstalled(File rootFile) {
        File[] buildFiles = findMathFile(rootFile, ConstantPool.BUILD_GRADLE_FILE);
        if (buildFiles != null && buildFiles.length == 1) {
            File rootBuildFile = buildFiles[0];
            int index = textInFile(rootBuildFile, ConstantPool.PLUGIN_NAME);
            if (index >= 0) {
                return true;
            }
        }
        return false;

    }

    private static boolean appInstalled(File rootFile) {
        File[] appFiles = findMathFile(rootFile, ConstantPool.MAIN_PROJECT_NAME);
        if (appFiles != null && appFiles.length == 1) {
            File appFile = appFiles[0];
            File appBuildFile = null;
            File[] appBuildFiles = findMathFile(appFile, ConstantPool.BUILD_GRADLE_FILE);
            if (appBuildFiles != null && appBuildFiles.length == 1) {
                appBuildFile = appBuildFiles[0];
                int index = textInFile(appBuildFile, ConstantPool.PLUGIN_APP_NAME);
                if (index >= 0) {
                    return true;
                }
            }
        }
        return false;
    }


    public static InstallResult checkPluginIsInstalled(File rootFile) {
        try {
            InstallResult installResult = new InstallResult();
            Properties props = new Properties();
            File f = new File(rootFile, ConstantPool.WINK_CONFIG);
            if (!f.getParentFile().exists())
                f.getParentFile().mkdirs();
            if (!f.exists())
                f.createNewFile();
            props.load(new FileInputStream(f));
            String appfilepath = props.getProperty(ConstantPool.LAST_MAIN_PATH);
            String rootfilepath = props.getProperty(ConstantPool.LAST_ROOT_PATH);

            if (null == appfilepath || appfilepath.length() == 0 || !new File(appfilepath).exists()) {
                installResult.appInstall = appInstalled(rootFile);
            } else {
                int appindex = textInFile(new File(appfilepath), ConstantPool.PLUGIN_APP_NAME);
                if (appindex >= 0) {
                    installResult.appInstall = true;
                    installResult.appInstallFile = appfilepath;
                    installResult.appInstallLineNum = appindex;
                } else {
                    installResult.appInstall = false;
                    installResult.appInstallFile = appfilepath;
                }
            }
            if (null == rootfilepath || rootfilepath.length() == 0 || !new File(rootfilepath).exists()) {
                installResult.rootInstall = rootInstalled(rootFile);
            } else {
                int rootindex = textInFile(new File(rootfilepath), ConstantPool.PLUGIN_NAME);
                if (rootindex >= 0) {
                    installResult.rootInstall = true;
                    installResult.rootInstallFile = appfilepath;
                    installResult.rootInstallLineNum = rootindex;
                } else {
                    installResult.rootInstall = false;
                    installResult.rootInstallFile = appfilepath;
                }
            }
            return installResult;
        } catch (Exception e) {
            NotificationUtils.errorNotification(Utils.getErrorString(e));
        }
        return null;
    }


    public static int textInFile(File file, String strSearch) {
        BufferedReader br = null;
        int result = -1;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            LineNumberReader reader = new LineNumberReader(br);
            String s = reader.readLine(); //定义行数
            int lines = 0;
            Stack<Boolean> stack = new Stack<Boolean>();
            while (s != null) //确定行数
            {
                if (s.contains("/*") && stack.empty()) {

                    stack.push(true);
                }
                String data = s.replaceAll(" ", "");
                if (data.contains(strSearch)) {
                    //被注释
                    if (data.startsWith("//") || data.contains("//" + strSearch)) {
                        break;
                    }
                    if (stack.empty()) {
                        result = lines;
                        return result;
                    }
                    //被 */ 注释包裹
                    break;
                }
                if (s.contains("*/") && !stack.empty()) {
                    stack.pop();
                }
                s = reader.readLine();
                lines++;
            }
            reader.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static SearchResult textInFileAndType(File file, String[] strSearch) {
        BufferedReader br = null;
        SearchResult searchResult = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            LineNumberReader reader = new LineNumberReader(br);
            String s = reader.readLine(); //定义行数
            int lines = 0;
            boolean haveSearch = false;
            while (s != null) //确定行数
            {
                for (int i = 0; i < strSearch.length; i++) {
                    if (s != null && s.contains(strSearch[i])) {
                        int type = i < 1 ? ConstantPool.PLUGINS_TYPE_NEW : ConstantPool.PLUGINS_TYPE_OLD;
                        searchResult = new SearchResult();
                        searchResult.searchFileName = file.getName();
                        searchResult.searchLineNum = lines;
                        searchResult.searchType = type;
                        haveSearch = true;
                    }
                }
                if (haveSearch) {
                    break;
                }
                s = reader.readLine();
                lines++;
            }
            reader.close();
            br.close();
        } catch (Exception e) {
            NotificationUtils.errorNotification(Utils.getErrorString(e));
        }
        return searchResult;
    }


    public static void installPlugin(File rootFile) throws Exception {
        File[] appFiles = findMathFile(rootFile, ConstantPool.MAIN_PROJECT_NAME);
        File[] buildFiles = findMathFile(rootFile, ConstantPool.BUILD_GRADLE_FILE);
        if (appFiles.length == 1 && buildFiles.length == 1) {
            File appFile = appFiles[0];
            File buildFile = buildFiles[0];
            File appBuildFile = null;
            File[] appBuildFiles = findMathFile(appFile, ConstantPool.BUILD_GRADLE_FILE);
            if (appBuildFiles.length == 1) {
                appBuildFile = appBuildFiles[0];
                int index = textInFile(buildFile, ConstantPool.CLASSPATH_MARK);
                //int index2 = textInFile(appBuildFile, "plugins {");
                SearchResult searchResult = textInFileAndType(appBuildFile, ConstantPool.getPluginMarks());
                if (searchResult == null) {
                    //"can't find plugin com.android.application node in file named:"+appBuildFile.getName()+" please check the file have node like: apply plugin: 'com.android.application' or id 'com.android.application'"
                    throw new Exception("can't not find com.android.application");
                }
                int indexHas = textInFile(buildFile, ConstantPool.PLUGIN_NAME);
                if (indexHas == -1) {
                    FileWinkUtils.insertNewLine(buildFile, new File(buildFile.getAbsolutePath() + ".temp"), ConstantPool.getPluginClassPath(), index);
                }
                int indexHas2 = textInFile(appBuildFile, ConstantPool.PLUGIN_APP_NAME);
                if (indexHas2 == -1) {
                    FileWinkUtils.insertNewLine(appBuildFile, new File(appBuildFile.getAbsolutePath() + ".temp"), ConstantPool.getPluginRegister(searchResult.searchType), searchResult.searchLineNum + 1);
                }
            }
        }
    }

    public static void saveResetBuildFile(File rootFile, File appBuildFile, File rootBuildFile) throws Exception {
        File backup = new File(rootFile, ConstantPool.IEAD_DIR);
        if (!backup.exists()) {
            backup.mkdirs();
        }
        File appBuildBack = new File(backup, appBuildFile.getParentFile().getName() + "_" + appBuildFile.getName() + ".temp");
        File rootBuildBack = new File(backup, rootBuildFile.getParentFile().getName() + "_" + rootBuildFile.getName() + ".temp");
        if (appBuildFile.exists()) {
            appBuildBack.createNewFile();
        }
        if (rootBuildBack.exists()) {
            rootBuildBack.createNewFile();
        }
        copyFileUsingFileStreams(appBuildFile, appBuildBack);
        copyFileUsingFileStreams(rootBuildFile, rootBuildBack);
    }

    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    public static boolean installPluginApp(File rootFile, File appBuildFile) throws Exception {
        SearchResult searchResult = textInFileAndType(appBuildFile, ConstantPool.getPluginMarks());
        if (searchResult == null) {
            NotificationUtils.errorNotification(appBuildFile.getAbsolutePath() + " is  not android Application build file type");
            return false;
        }
        int indexHas2 = textInFile(appBuildFile, ConstantPool.PLUGIN_APP_NAME);
        if (indexHas2 == -1) {
            FileWinkUtils.insertNewLine(appBuildFile, new File(appBuildFile.getAbsolutePath() + ".temp"), ConstantPool.getPluginRegister(searchResult.searchType), searchResult.searchLineNum + 1);
        }
        saveToPro(rootFile, ConstantPool.LAST_MAIN_PATH, appBuildFile.getAbsolutePath());
        return true;
    }

    public static boolean installPluginRoot(File rootFile, File rootBuildFile) throws Exception {
        int index = textInFile(rootBuildFile, ConstantPool.CLASSPATH_MARK);
        int indexHas = textInFile(rootBuildFile, ConstantPool.PLUGIN_NAME);
        if (indexHas == -1) {
            FileWinkUtils.insertNewLine(rootBuildFile, new File(rootBuildFile.getAbsolutePath() + ".temp"), ConstantPool.getPluginClassPath(), index);
        }
        saveToPro(rootFile, ConstantPool.LAST_ROOT_PATH, rootBuildFile.getAbsolutePath());
        return true;
    }

    public static synchronized void saveToPro(File rootFile, String key, String value) throws Exception {
        Properties props = new Properties();
        File propsFile = new File(rootFile, ConstantPool.WINK_CONFIG);
        if (!propsFile.getParentFile().exists()) {
            propsFile.getParentFile().mkdirs();
        }
        if (!propsFile.exists()) {
            propsFile.createNewFile();
        }
        props.load(new FileInputStream(propsFile));
        props.setProperty(key, value);
        props.store(new FileOutputStream(propsFile), "save " + value);
    }

    public static synchronized String getValuePro(File rootFile, String key) throws Exception {
        Properties props = new Properties();
        File propsFile = new File(rootFile, ConstantPool.WINK_CONFIG);
        if (!propsFile.getParentFile().exists()) {
            propsFile.getParentFile().mkdirs();
        }
        if (!propsFile.exists()) {
            propsFile.createNewFile();
        }
        props.load(new FileInputStream(propsFile));
        return props.getProperty(key);
    }


    public static void insertNewLine(File srcFile, File temp, String insertContent, int line) {
        try {
            if (srcFile.exists()) {
                RandomAccessFile read = new RandomAccessFile(srcFile, "rw");
                RandomAccessFile insert = new RandomAccessFile(temp, "rw");
                String str = "";
                int index = 0;

                while (null != (str = read.readLine())) {
                    if (index == line) {//等于写入行号时
                        insert.write((insertContent + "\n").getBytes());//写入新内容+原有内容
                        insert.write((str + "\n").getBytes());//写
                    } else {
                        insert.write((str + "\n").getBytes());//写入原有内容
                    }
                    index++;
                }

                if (index < line) {//行号大于文件行数,在文件末位处添加内容
                    long length = temp.length();//原有文件长度
                    insert.seek(length);
                    insert.write(insertContent.getBytes());//写入文件末尾处
                }

                insert.close();
                read.close();
                read = new RandomAccessFile(srcFile, "rw");

                insert = new RandomAccessFile(temp, "rw");
                while (null != (str = insert.readLine())) {//将临时文件内容写到源文件
                    read.write((str + "\n").getBytes());
                }

                read.close();
                insert.close();
                temp.delete();//删除临时文件
            }
        } catch (Exception e) {
            NotificationUtils.errorNotification(Utils.getErrorString(e));
        }
    }


    private static class SearchResult {
        public int searchLineNum;
        public int searchType;
        public String searchFileName;
    }

    public static class InstallResult {
        public boolean appInstall;
        public boolean rootInstall;
        public int appInstallLineNum = -1;
        public String appInstallFile;
        public String rootInstallFile;
        public int rootInstallLineNum = -1;


        @Override
        public String toString() {
            return "InstallResult{" +
                    "appInstall=" + appInstall +
                    ", rootInstall=" + rootInstall +
                    ", appInstallLineNum=" + appInstallLineNum +
                    ", appInstallFile='" + appInstallFile + '\'' +
                    ", rootInstallFile='" + rootInstallFile + '\'' +
                    ", rootInstallLineNum=" + rootInstallLineNum +
                    '}';
        }
    }

}
