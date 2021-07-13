package com.immomo.wink.utils;

import com.immomo.wink.model.ProjectInfo;
import com.immomo.wink.views.WinkBuild;
import com.immomo.wink.views.WinkBuildDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by pengwei on 2016/11/2.
 */
public final class DialogUtil {

    /**
     * 创建普通对话框
     * @param message
     * @param okText
     * @param cancelText
     * @return
     */
    public static boolean createDialog(String message, String okText, String cancelText,Action action) {
        DialogBuilder builder = new DialogBuilder();
        builder.setTitle("Dialog Message");
        builder.resizable(false);
        builder.setCenterPanel(new JLabel(message, Messages.getInformationIcon(), SwingConstants.CENTER));
        builder.addOkAction().setText(okText);
        builder.addCancelAction().setText(cancelText);
        builder.addAction(action);
        builder.setButtonsAlignment(SwingConstants.CENTER);
        return  builder.show() == 0;
    }



    public static void showInstallDialog(File rootFile, Project project, FileWinkUtils.InstallResult installResult) {
        //WindowManager.getInstance().getFrame(project).wind
        WinkBuild dialog = new WinkBuild(rootFile,project,installResult);
        dialog.pack();
        dialog.setLocationRelativeTo(getParentWindow(project));
        dialog.setVisible(true);
    }

    private static Window getParentWindow(Project project) {
        WindowManagerEx windowManager = (WindowManagerEx) WindowManager.getInstance();

        Window window = windowManager.suggestParentWindow(project);
        if (window == null) {
            Window focusedWindow = windowManager.getMostRecentFocusedWindow();
            if (focusedWindow instanceof IdeFrameImpl) {
                window = focusedWindow;
            }
        }
        return window;
    }

    public static void showBuildInstallDialog(ProjectInfo project) {
        //WindowManager.getInstance().getFrame(project).wind
        WinkBuildDialog dialog = new WinkBuildDialog(project);
        dialog.pack();
        dialog.setLocationRelativeTo(getParentWindow(project.getCurrentProject()));
        dialog.setVisible(true);
    }
}
