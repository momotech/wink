package com.immomo.wink.model;

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel;

public class ModuleInfo {
    private String moduleName;
    private ModuleType moduleType;
    private GradleBuildModel gradleBuild;
    private boolean installPlugin;
    public ModuleInfo(){}

    public ModuleInfo(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public GradleBuildModel getGradleBuild() {
        return gradleBuild;
    }

    public void setGradleBuild(GradleBuildModel gradleBuild) {
        this.gradleBuild = gradleBuild;
    }

    public boolean isInstallPlugin() {
        return installPlugin;
    }

    public void setInstallPlugin(boolean installPlugin) {
        this.installPlugin = installPlugin;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public enum ModuleType{
        ROOT,LIB,APP;
    }
}
