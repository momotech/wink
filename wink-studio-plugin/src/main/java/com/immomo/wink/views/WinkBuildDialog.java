package com.immomo.wink.views;

import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel;
import com.immomo.wink.ConstantPool;
import com.immomo.wink.model.DataManager;
import com.immomo.wink.model.ModuleInfo;
import com.immomo.wink.model.ProjectInfo;
import com.immomo.wink.model.VersionInfo;
import com.immomo.wink.utils.FileWinkUtils;
import com.immomo.wink.utils.NotificationUtils;
import com.immomo.wink.utils.Utils;
import com.intellij.openapi.command.WriteCommandAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WinkBuildDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JLabel link;
    private JLabel main_app_label;
    private JLabel install_helper;
    private JLabel root_label;
    private JComboBox comboBox3;
    ProjectInfo projectInfo;

    private File rootFile;

    private HashMap<String, ModuleInfo> appBuildMap;
    private HashMap<String, ModuleInfo> rootBuildMap;

    public WinkBuildDialog(ProjectInfo projectInfo) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.projectInfo = projectInfo;
        this.rootFile = new File(projectInfo.getBaseDir());
        main_app_label.setText(ConstantPool.UI_WINK_MAIN_BUILD);
        root_label.setText(ConstantPool.UI_WINK_ROOT_BUILD);
        install_helper.setText(ConstantPool.INSTALL_HELPER);
        initSelect(rootFile, projectInfo);
        buttonOK.setEnabled(false);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onInstall();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        initLink();
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void initLink() {
        link.setText(ConstantPool.getLinkAddress());
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    open(new URI(ConstantPool.USER_LINK));
                } catch (URISyntaxException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
            }
        });
    }

    private void initSelect(File rootFile, ProjectInfo projectInfo) {
        try {
            appBuildMap = new HashMap<>();
            rootBuildMap = new HashMap<>();
            ArrayList<String> appaths = new ArrayList<>();
            if (projectInfo.getLastAppModule() != null && !projectInfo.getLastAppModule().equals("")) {
                String lastAppName = projectInfo.getLastAppModule();
                for (ModuleInfo moduleInfo : projectInfo.getSubAppModules()) {
                    String shortPath = moduleInfo.getGradleBuild().getVirtualFile().getPath().replace(projectInfo.getBaseDir(), "");
                    if (lastAppName.equals(moduleInfo.getModuleName()) && moduleInfo.isInstallPlugin()) {
                        appaths.add(0, shortPath);
                    } else {
                        appaths.add(shortPath);
                    }
                    appBuildMap.put(shortPath, moduleInfo);
                }
            } else {
                for (ModuleInfo moduleInfo : projectInfo.getSubAppModules()) {
                    String shortPath = moduleInfo.getGradleBuild().getVirtualFile().getPath().replace(projectInfo.getBaseDir(), "");
                    if (moduleInfo.isInstallPlugin()) {
                        appaths.add(0, shortPath);
                    } else {
                        appaths.add(shortPath);
                    }
                    appBuildMap.put(shortPath, moduleInfo);
                }
            }
            ArrayList<String> paths = new ArrayList<>();
            String shortPath = projectInfo.getRootModule().getGradleBuild().getVirtualFile().getPath().replace(projectInfo.getBaseDir(), "");
            paths.add(shortPath);
            rootBuildMap.put(shortPath, projectInfo.getRootModule());

            setJCBoxData(comboBox1, appaths);
            setJCBoxData(comboBox2, paths);
        } catch (Exception e) {
            NotificationUtils.errorNotification(e.getMessage());
        }

        DataManager.getInstance().getVersion(projectInfo.getCurrentProject(), new VersionInfo.VersionCallBack() {
            @Override
            public void onVersionReceive(String ideaVersion, List<String> pluginVersions) {
                if (pluginVersions != null && pluginVersions.size() > 0) {
                    setJCBoxData(comboBox3, pluginVersions);
                } else {
                    List<String> defaultList = new ArrayList<>();
                    defaultList.add(ConstantPool.PLUGIN_VERSION);
                    setJCBoxData(comboBox3, defaultList);
                }
                buttonOK.setEnabled(true);
            }
        });

    }


    private void setJCBoxData(JComboBox jcBox, List<String> paths) {
        if (jcBox != null) {
            jcBox.removeAllItems();
            for (String str : paths) {
                jcBox.addItem(str);
            }
        }
    }


    private void onInstall() {
        WriteCommandAction.runWriteCommandAction(projectInfo.getCurrentProject(), new Runnable() {
            @Override
            public void run() {
                try {
                    String apppath = (String) comboBox1.getSelectedItem();
                    String selectVersion = (String) comboBox3.getSelectedItem();
                    ConstantPool.PLUGIN_VERSION = selectVersion;

                    ModuleInfo rootModule = projectInfo.getRootModule();
                    if (!rootModule.isInstallPlugin()) {
                        DependenciesModel dependenciesModel = rootModule.getGradleBuild().buildscript().dependencies();
                        boolean haveClassPath = dependenciesModel.containsArtifact("classpath", ConstantPool.pluginClassPathDependence());
                        if (!haveClassPath) {
                            dependenciesModel.addArtifact("classpath", ConstantPool.pluginClassPathDependence());
                        }
                        rootModule.getGradleBuild().applyChanges();
                    }

                    ModuleInfo moduleInfo = appBuildMap.get(apppath);
                    if (!moduleInfo.isInstallPlugin()) {
                        moduleInfo.getGradleBuild().applyPlugin(ConstantPool.PLUGIN_APP_NAME);
                        FileWinkUtils.saveToPro(rootFile, ConstantPool.LAST_INSTALL_MODULE, moduleInfo.getModuleName());
                        moduleInfo.getGradleBuild().applyChanges();
                    }
                    DataManager.getInstance().updateWinkInstall(projectInfo.getCurrentProject());
                    NotificationUtils.infoNotification("WinkBuild Install Success");
                } catch (Exception e) {
                    //NotificationUtils.infoNotification("WinkBuild Install Failed");
                    NotificationUtils.infoNotification(Utils.getErrorString("WinkBuild Install Failed",e));
                }
            }
        });
        dispose();
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onReset() {
        // add your code here if necessary
        dispose();
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to launch the link, your computer is likely misconfigured.",
                        "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Java is not able to launch links on your computer.",
                    "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        //WinkBuild dialog = new WinkBuild();
        //dialog.pack();
        //dialog.setVisible(true);
    }
}
