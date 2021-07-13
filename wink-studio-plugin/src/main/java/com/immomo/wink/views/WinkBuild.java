package com.immomo.wink.views;

import com.immomo.wink.ConstantPool;
import com.immomo.wink.model.DataManager;
import com.immomo.wink.model.VersionInfo;
import com.immomo.wink.utils.FileWinkUtils;
import com.immomo.wink.utils.NotificationUtils;
import com.immomo.wink.utils.Utils;
import com.intellij.openapi.project.Project;

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

public class WinkBuild extends JDialog {
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
    FileWinkUtils.InstallResult installResult;

    private File rootFile;

    private HashMap<String,File> appBuildMap;
    private HashMap<String,File> rootBuildMap;

    public WinkBuild(File rootFile, Project project, FileWinkUtils.InstallResult installResult) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.installResult = installResult;
        this.rootFile = rootFile;
        main_app_label.setText(ConstantPool.UI_WINK_MAIN_BUILD);
        root_label.setText(ConstantPool.UI_WINK_ROOT_BUILD);
        install_helper.setText(ConstantPool.INSTALL_HELPER);
        initSelect(rootFile,project);
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

    private void initLink(){
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

    private void initSelect(File rootFile,Project project){
        try {
            String rootPath = rootFile.getParent();
            appBuildMap = new HashMap<>();
            rootBuildMap = new HashMap<>();
            ArrayList<File> childBuildFile = FileWinkUtils.findRootProjectBuild(rootFile);
            ArrayList<String> appaths = new ArrayList<>();
            for(File item : childBuildFile){
                String path =item.getAbsolutePath().replace(rootPath,"");
                appBuildMap.put(path,item);
                if(path.contains(ConstantPool.MAIN_PROJECT_NAME+"/")){
                    appaths.add(0,path);
                }else {
                    appaths.add(path);
                }
            }

            File[] buildFiles = FileWinkUtils.findMathFile(rootFile, ConstantPool.BUILD_GRADLE_FILE);
            ArrayList<String> paths = new ArrayList<>();
            for(File item : buildFiles){
                String path =item.getAbsolutePath().replace(rootPath,"");
                rootBuildMap.put(path,item);
                paths.add(path);
            }
            setJCBoxData(comboBox1,appaths);
            setJCBoxData(comboBox2,paths);
        }catch (Exception e){
            NotificationUtils.errorNotification(Utils.getErrorString(e));
        }
        DataManager.getInstance().getVersion(project, new VersionInfo.VersionCallBack() {
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


    private void setJCBoxData(JComboBox jcBox, List<String> paths){
        if(jcBox!=null){
            jcBox.removeAllItems();
            for(String str:paths){
                jcBox.addItem(str);
            }
        }
    }


    private void onInstall() {
        try{
            boolean installAppSuccess = installResult.appInstall;
            boolean installRootSuccess = installResult.rootInstall;
            String apppath = (String) comboBox1.getSelectedItem();
            String rootpath = (String) comboBox2.getSelectedItem();
            String selectVersion = (String) comboBox3.getSelectedItem();
            ConstantPool.PLUGIN_VERSION = selectVersion;
            FileWinkUtils.saveResetBuildFile(rootFile,appBuildMap.get(apppath), rootBuildMap.get(rootpath));
            if(installResult!=null){
                if(!installResult.appInstall){
                    installAppSuccess = FileWinkUtils.installPluginApp(rootFile,appBuildMap.get(apppath));
                }
                if(!installResult.rootInstall){
                    installRootSuccess = FileWinkUtils.installPluginRoot(rootFile,rootBuildMap.get(rootpath));
                }
            }else {
                installAppSuccess = FileWinkUtils.installPluginApp(rootFile,appBuildMap.get(apppath));
                installRootSuccess = FileWinkUtils.installPluginRoot(rootFile,rootBuildMap.get(rootpath));
            }
            if(installAppSuccess &&  installRootSuccess){
                NotificationUtils.infoNotification("WinkBuild Install Success");
            }else {
                NotificationUtils.infoNotification("WinkBuild Install Failed");
            }
        }catch (Exception e){
            NotificationUtils.errorNotification(Utils.getErrorString(e));
        }
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
                        "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
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
