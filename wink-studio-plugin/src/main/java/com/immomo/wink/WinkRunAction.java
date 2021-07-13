package com.immomo.wink;

import com.immomo.wink.icons.PluginIcons;
import com.immomo.wink.model.DataManager;
import com.immomo.wink.model.ModuleInfo;
import com.immomo.wink.model.ProjectInfo;
import com.immomo.wink.utils.*;
import com.immomo.wink.views.WinkTerminal;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;

public class WinkRunAction extends BaseAction {
    private static final Logger log = Logger.getInstance(WinkRunAction.class);

    public WinkRunAction() {
        super(PluginIcons.WinkIcon);
    }

    @Override
    public void actionPerformed() {
        runWinkUseGradle();
    }


    private void runWinkUseGradle() {
        try {
            DocumentUtil.saveDocument();
            runWinkCompile();

//            ProjectInfo projectInfo = DataManager.getInstance().checkWinkInstall(currentProject);
//            if (projectInfo != null) {
//                boolean isRootInstall = projectInfo.getRootModule().isInstallPlugin();
//                boolean isAppInstall = false;
//                if (projectInfo.getLastAppModule() != null && !projectInfo.getLastAppModule().equals("")) {
//                    String lastAppName = projectInfo.getLastAppModule();
//                    for (ModuleInfo moduleInfo : projectInfo.getSubAppModules()) {
//                        if (lastAppName.equals(moduleInfo.getModuleName()) && moduleInfo.isInstallPlugin()) {
//                            isAppInstall = true;
//                        }
//                    }
//                }
//                if (isRootInstall && isAppInstall) {
//                    runWinkCompile();
//                    return;
//                }
//            }
//            DialogUtil.showBuildInstallDialog(projectInfo);
        } catch (Exception e) {
            NotificationUtils.infoNotification(Utils.getErrorString(e));
        }

    }

    private void runWinkWithFile() {
        FileWinkUtils.InstallResult installResult = FileWinkUtils.checkPluginIsInstalled(projectDir);
        NotificationUtils.infoNotification(installResult.toString());

        if (installResult.appInstall && installResult.rootInstall) {
            WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND);
        } else {
            DialogUtil.showInstallDialog(projectDir, currentProject, installResult);
        }
    }


    private void runWinkCompile() {
        if (currentProject != null) {
            File shellFile = new File(currentProject.getBasePath()+File.separator+ConstantPool.IDEA_WINK_LIB_DIR, ConstantPool.WINK_SHELL);
            if (shellFile.exists() && shellFile.isFile()) {
                shellFile.setExecutable(true,false);
                WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_SHELL);
            }else {
                WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_INIT +" && "+ConstantPool.COMMAND_SHELL );
                //WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_SHELL);
            }
        }
       // WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND);
    }


}
