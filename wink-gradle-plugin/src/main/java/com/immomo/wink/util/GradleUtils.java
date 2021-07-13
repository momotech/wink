package com.immomo.wink.util;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.immomo.wink.Settings;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

import java.io.File;
import java.util.Set;

public class GradleUtils {
    public static boolean isStableFileExist(Project project) {
        String path = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME + "/stableIds.txt";
        File f = new File(path);
        return f.exists();
    }

    public static Task getFlavorTask(Project project, String pre, String post){
        //preDebugBuild
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if(flavors!=null && flavors.getNames().size()>0){
            Set<String> flavorNames = flavors.getNames();
            for(String name:flavorNames){
                String processDebugResources = pre + Utils.upperCaseFirst(name) + post;
                try {
                    Task targetTask = project.getTasks().getByName(processDebugResources);
                    return targetTask;
                }catch (UnknownTaskException e){
                    e.printStackTrace();
                }
            }
        }

        return project.getTasks().getByName(pre + post);
    }
}
