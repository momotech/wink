package com.immomo.wink.listener;

import com.immomo.wink.ConstantPool;
import com.immomo.wink.model.DataManager;
import com.immomo.wink.model.VersionInfo;
import com.immomo.wink.utils.FileWinkUtils;
import com.immomo.wink.utils.NotificationUtils;
import com.immomo.wink.utils.Utils;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProjectListener implements ProjectManagerListener {
    @Override
    public void projectOpened(@NotNull Project project) {
        checkWinkProject(project);
        checkVersion(project);
    }
    private void checkWinkProject(Project project){
        File winkDir = new File(project.getBasePath(),ConstantPool.IDEA_WINK_DIR);
        if(!winkDir.exists() && !winkDir.isDirectory()){
            winkDir.mkdirs();
        }
        DataManager.getInstance().checkWinkInstall(project);
    }

    private void checkNeedJars(Project project){
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "WinkBuild jar check") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                File jarDir = new File(project.getBasePath(),ConstantPool.IEAD_WINK_JAR);
                Map<String,String> stringHashMap = Utils.stringArrayToMap(ConstantPool.JAR_NAMES);
                if(jarDir.exists()&&jarDir.isDirectory()){
                    for(File item:jarDir.listFiles()){
                        if(stringHashMap.get(item.getName())==null){
                            copyJar(item.getName(),project.getBasePath());
                        }
                    }
                }else{
                    jarDir.mkdirs();
                    for(String item:ConstantPool.JAR_NAMES){
                        copyJar(item,project.getBasePath());
                    }
                }
            }
        });
    }



    private void checkVersion(Project project){
        try {
            if(isSameDay(project)){
                return;
            }
            DataManager.getInstance().getVersion(project, new VersionInfo.VersionCallBack() {
                @Override
                public void onVersionReceive(String ideaVersion, List<String> pluginVersions) {
                    if(ideaVersion!=null && ideaVersion.length()>0){
                        if(!ConstantPool.CURRENT_IDEA_VERSION.equals(ideaVersion) ){
                            NotificationUtils.infoNotification("WinkBuild have a new version!");
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isSameDay(Project project) throws Exception{
        File rootFile = new File(project.getBasePath());
        Date date = new Date();
        String strDateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        String now = sdf.format(date);
        String value = FileWinkUtils.getValuePro(rootFile,ConstantPool.LAST_CHECK_DATA);
        if(now.equals(value)){
            return true;
        }else {
            FileWinkUtils.saveToPro(rootFile,ConstantPool.LAST_CHECK_DATA,now);
            return false;
        }
    }

    private void copyJar(String jarName,String projectDir){
        try {
            InputStream jarStream = getClass().getResourceAsStream("/winkjars/"+jarName);
            File jarDir = new File(projectDir,ConstantPool.IEAD_WINK_JAR);
            if(!jarDir.exists()){
                jarDir.mkdirs();
            }
            File targetJarFile = new File(jarDir,jarName);
            if(!targetJarFile.exists()){
                targetJarFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(targetJarFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = jarStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.close();
            jarStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
