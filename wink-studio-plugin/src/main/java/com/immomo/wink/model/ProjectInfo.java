package com.immomo.wink.model;

import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {
    private Project currentProject;
    private String projectName;
    private String baseDir;

    private ModuleInfo rootModule;
    private List<ModuleInfo> subAppModules = new ArrayList<>();
    private List<ModuleInfo> subLibModules = new ArrayList<>();

    private String lastAppModule;

    public ProjectInfo(Project currentProject) {
        this.currentProject = currentProject;
        this.baseDir = currentProject.getBasePath();
        this.projectName = currentProject.getName();
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public ModuleInfo getRootModule() {
        return rootModule;
    }

    public void setRootModule(ModuleInfo rootModule) {
        this.rootModule = rootModule;
    }

    public List<ModuleInfo> getSubAppModules() {
        return subAppModules;
    }

    public void addSubAppModules(ModuleInfo subAppModule) {
        if(isMainApp(subAppModule)){
            this.subAppModules.add(0,subAppModule);
            return;
        }
        this.subAppModules.add(subAppModule);
    }

    private boolean isMainApp(ModuleInfo subAppModule){
        if(subAppModule!=null &&  subAppModule.getModuleName()!=null){
            return subAppModule.getModuleName().contains(".app");
        }
        return false;
    }


    public List<ModuleInfo> getSubLibModules() {
        return subLibModules;
    }

    public void addSubLibModules(ModuleInfo subLibModule) {
        this.subLibModules.add(subLibModule);
    }

    public String getLastAppModule() {
        return lastAppModule;
    }

    public void setLastAppModule(String lastAppModule) {
        this.lastAppModule = lastAppModule;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "currentProject=" + currentProject +
                ", projectName='" + projectName + '\'' +
                ", baseDir='" + baseDir + '\'' +
                ", rootModule=" + rootModule +
                ", subAppModules=" + subAppModules +
                ", subLibModules=" + subLibModules +
                ", lastAppModule='" + lastAppModule + '\'' +
                '}';
    }
}
