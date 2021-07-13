package com.immomo.wink;

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel;
import com.android.tools.idea.gradle.dsl.api.PluginModel;
import com.android.tools.idea.gradle.dsl.api.ProjectBuildModel;
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencyModel;
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel;
import com.immomo.wink.model.ModuleInfo;
import com.immomo.wink.model.ProjectInfo;
import com.immomo.wink.utils.GradleUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Created by pengwei on 16/9/11.
 */
public abstract class BaseAction extends AnAction {

    protected Project currentProject;
    protected File projectDir;
    protected AnActionEvent anActionEvent;

    public BaseAction(Icon icon) {
        super(icon);
    }

    @Override
    public final void actionPerformed(AnActionEvent anActionEvent) {
        this.anActionEvent = anActionEvent;
        this.currentProject = DataKeys.PROJECT.getData(anActionEvent.getDataContext());
        this.projectDir = new File(currentProject.getBasePath());
        //test1(anActionEvent);
        actionPerformed();
    }

    public void test1(AnActionEvent anActionEvent){
        final Project project = anActionEvent.getProject();
        String projectName = project.getName();
        ProjectInfo projectInstallInfo = new ProjectInfo(project);

        ProjectBuildModel projectBuildModel = ProjectBuildModel.get(project);
        GradleBuildModel rootBuild= projectBuildModel.getProjectBuildModel();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            GradleBuildModel buildModel = projectBuildModel.getModuleBuildModel(module);
            //工程module
            if(GradleUtil.isSameBuildMode(rootBuild,buildModel)){
                ModuleInfo rootModule = new ModuleInfo(ModuleInfo.ModuleType.ROOT);
                rootModule.setGradleBuild(buildModel);
                DependenciesModel dependenciesModel  = buildModel.buildscript().dependencies();
                List<ArtifactDependencyModel> dependencyModelList = dependenciesModel.artifacts();
                for(ArtifactDependencyModel model:dependencyModelList){
                    String dependModuleName  = model.group()+":"+model.name().toString();
                    if(dependModuleName.equals(ConstantPool.PLUGIN_NAME)){
                        rootModule.setInstallPlugin(true);
                    }
                }
                projectInstallInfo.setRootModule(rootModule);
                //dependenciesModel.containsArtifact("classpath", ArtifactDependencySpec.create("gradle","com.android.tools.build","4.0.0"));
            }
            //子module
            else {
                List<PluginModel> pluginModelList = buildModel.plugins();
                ModuleInfo subModule = new ModuleInfo(ModuleInfo.ModuleType.LIB);
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
                    projectInstallInfo.addSubAppModules(subModule);
                }else {
                    projectInstallInfo.addSubLibModules(subModule);
                }
            }
        }
    }

    public abstract void actionPerformed();


    /**
     * 异步执行
     *
     * @param runnable
     */
    protected void asyncTask(Runnable runnable) {
        ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }

    protected void invokeLater(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(runnable);
    }


}
