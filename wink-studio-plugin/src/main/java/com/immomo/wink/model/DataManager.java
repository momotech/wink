package com.immomo.wink.model;

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel;
import com.android.tools.idea.gradle.dsl.api.PluginModel;
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel;
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel;
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel;
import com.immomo.wink.ConstantPool;
import com.immomo.wink.utils.FileWinkUtils;
import com.immomo.wink.utils.GradleUtil;
import com.immomo.wink.utils.NotificationUtils;
import com.immomo.wink.utils.Utils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static DataManager dataManager;
    private Map<String,ProjectInfo> projectInfoMap = new HashMap<>();
    private VersionInfo cacheInfo;
    private boolean needUpdate = true;
    private DataManager(){

    }

    public static DataManager getInstance() {
        if(dataManager==null){
            synchronized (DataManager.class){
                if(dataManager==null){
                    dataManager = new DataManager();
                }
            }
        }
        return dataManager;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public ProjectInfo updateWinkInstall(Project project){
        needUpdate = true;
        return checkWinkInstall(project);
    }
    public ProjectInfo checkWinkInstall(Project project){
        String projectKey = project.toString();
        ProjectInfo projectInfo = projectInfoMap.get(projectKey);
        if(!needUpdate && projectInfo!=null){
            return projectInfo;
        }
        try {
            needUpdate = false;
            projectInfo = new ProjectInfo(project);
            ProjectBuildModel projectBuildModel = ProjectBuildModel.get(project);
            GradleBuildModel rootBuild= projectBuildModel.getProjectBuildModel();
            Module[] modules = ModuleManager.getInstance(project).getModules();
            String lastInstallModule = null;
            try {
                lastInstallModule =  FileWinkUtils.getValuePro(new File(project.getBasePath()),ConstantPool.LAST_INSTALL_MODULE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Module module : modules) {
                GradleBuildModel buildModel = projectBuildModel.getModuleBuildModel(module);
                if(buildModel==null){
                    continue;
                }
                //工程module
                if(GradleUtil.isSameBuildMode(rootBuild,buildModel)){
                    ModuleInfo rootModule = new ModuleInfo(ModuleInfo.ModuleType.ROOT);
                    rootModule.setGradleBuild(buildModel);
                    rootModule.setModuleName(module.getName());
                    DependenciesModel dependenciesModel  = buildModel.buildscript().dependencies();
                    List<ArtifactDependencyModel> dependencyModelList = dependenciesModel.artifacts();
                    for(ArtifactDependencyModel model:dependencyModelList){
                        String dependModuleName  = model.group()+":"+model.name().toString();
                        if(dependModuleName.equals(ConstantPool.PLUGIN_NAME)){
                            rootModule.setInstallPlugin(true);
                        }
                    }
                    if(!rootModule.isInstallPlugin()){
                        boolean rootInstall = FileWinkUtils.textInFile(new File(rootModule.getGradleBuild().getVirtualFile().getPath()),ConstantPool.PLUGIN_NAME)>0;
                        rootModule.setInstallPlugin(rootInstall);
                    }
                    projectInfo.setRootModule(rootModule);
                    //dependenciesModel.containsArtifact("classpath", ArtifactDependencySpec.create("gradle","com.android.tools.build","4.0.0"));
                }
                //子module
                else {
                    List<PluginModel> pluginModelList = buildModel.plugins();
                    ModuleInfo subModule = new ModuleInfo(ModuleInfo.ModuleType.LIB);
                    subModule.setModuleName(module.getName());
                    subModule.setGradleBuild(buildModel);
                    boolean isAppModule = false;
                    for(PluginModel pluginModel:pluginModelList){
                        if(ConstantPool.ANDROID_APP_MARK.equals(pluginModel.name().toString())){
                            subModule.setModuleType(ModuleInfo.ModuleType.APP);
                            isAppModule = true;
                        }
                        if(pluginModel.name().toString().contains(ConstantPool.PLUGIN_APP_NAME)){
                            subModule.setInstallPlugin(true);
                        }
                    }

                    if(isAppModule){
                        if(!subModule.isInstallPlugin()){
                            boolean rootInstall = FileWinkUtils.textInFile(new File(subModule.getGradleBuild().getVirtualFile().getPath()),ConstantPool.PLUGIN_APP_NAME)>0;
                            subModule.setInstallPlugin(rootInstall);
                        }else {
                            if(lastInstallModule==null || lastInstallModule.trim().length()==0){
                                lastInstallModule = subModule.getModuleName();
                            }
                        }
                        projectInfo.addSubAppModules(subModule);
                    }else {
                        projectInfo.addSubLibModules(subModule);
                    }
                }
            }
            projectInfo.setLastAppModule(lastInstallModule);
            projectInfoMap.put(projectKey,projectInfo);
            return projectInfo;
        }catch (Exception e){
            NotificationUtils.infoNotification(Utils.getErrorString(e));
        }
        return null;
    }


    public synchronized void getVersion(Project project, VersionInfo.VersionCallBack versionCallBack){
        try {
            if(cacheInfo!=null) {
                if(versionCallBack!=null){
                    versionCallBack.onVersionReceive(cacheInfo.ideaVersion,cacheInfo.pluginVersion);
                }
            }
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "WinkBuild check") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        String version = HttpRequests.request(ConstantPool.UPDATE_URL).readString(indicator);
                        JSONObject jsonObject  = new JSONObject(version);
                        String serverVersion = jsonObject.getString(ConstantPool.IDEA_VERSION);
                        JSONArray plugins = jsonObject.getJSONArray(ConstantPool.P_VERSION);
                        List<String> pluginVersions = new ArrayList<>();
                        if(plugins!=null){
                            for(int i=0;i<plugins.length();i++){
                                pluginVersions.add(plugins.getString(i));
                            }
                        }
                        if(cacheInfo==null){
                            cacheInfo = new VersionInfo(serverVersion,pluginVersions);
                        }else {
                            cacheInfo.pluginVersion = pluginVersions;
                            cacheInfo.ideaVersion = serverVersion;
                        }
                        if(versionCallBack!=null){
                            versionCallBack.onVersionReceive(serverVersion,pluginVersions);
                        }
                    } catch (Exception e) {
                        if(versionCallBack!=null){
                            versionCallBack.onVersionReceive(ConstantPool.CURRENT_IDEA_VERSION,null);
                        }
                    }
                }
            });
        } catch (Exception e){
            if(versionCallBack!=null){
                versionCallBack.onVersionReceive(ConstantPool.CURRENT_IDEA_VERSION,null);
            }
        }
    }

    public void checkNeedJars(){}


}
