package com.immomo.wink.views;

import com.immomo.wink.ConstantPool;
import com.immomo.wink.icons.PluginIcons;
import com.immomo.wink.utils.DocumentUtil;
import com.immomo.wink.utils.NotificationUtils;
import com.immomo.wink.utils.Utils;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.model.TerminalTextBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Created by pengwei on 16/9/15.
 */
public class WinkTerminal implements FocusListener, ProjectComponent {

    private @NotNull JBTerminalWidget myTerminalWidget;
    private Project myProject;
    private static final Logger log = Logger.getInstance(WinkTerminal.class);
    public WinkTerminal(Project project) {
        this.myProject = project;
    }

    public static WinkTerminal getInstance(Project project) {
        return project.getComponent(WinkTerminal.class);
    }

    public JBTerminalWidget getTerminalWidget(ToolWindow window) {
        window.show(null);
        if (myTerminalWidget == null) {
            JComponent parentPanel =  window.getContentManager().getContents()[0].getComponent();
            if (parentPanel instanceof SimpleToolWindowPanel) {
                SimpleToolWindowPanel panel = (SimpleToolWindowPanel) parentPanel;
                JPanel jPanel = (JPanel) panel.getComponents()[0];
                myTerminalWidget = (JBTerminalWidget) jPanel.getComponents()[0];
            } else {
                NotificationUtils.infoNotification("Wait for Freeline to initialize");
            }
        }
        return myTerminalWidget;
    }

    public JBTerminalWidget getTerminalWidget() {
        ToolWindow window = getToolWindow();
        return getTerminalWidget(window);
    }

    public JBTerminalWidget getCurrentSession() {
        if (getTerminalWidget() != null) {
            return (JBTerminalWidget) getTerminalWidget().getCurrentSession();
        }
        return null;
    }

    /**
     * 在terminal输入shell
     */
    private void sendString(String shell) {
        if (getCurrentSession() != null) {
            System.out.println("executeShell  getCurrentSession sendString success");
            getCurrentSession().getTerminalStarter().sendString(shell);
        }
    }

    public void initAndExecute(String shell) {
        ToolWindow toolWindow = getToolWindow();
        if (toolWindow.isActive()) {
            System.out.println("toolWindow.isActive()");
            executeShell(shell);
        } else {
            System.out.println("toolWindow.isNotActive()");
            toolWindow.activate(new Runnable() {
                @Override
                public void run() {
                    executeShell(shell);
                }
            });
        }
    }

    /**
     * 执行shell
     * 利用terminal换行即执行原理
     *
     * @param shell
     */
    public void executeShell(String shell) {
        if (getCurrentSession() != null) {
            log.info("executeShell  getCurrentSession is not null");
            TerminalTextBuffer buffer = getTerminalWidget().getCurrentSession().getTerminalTextBuffer();
            String lastLineText = buffer.getLine(buffer.getScreenLinesCount() - 1).getText().trim();
            shell = shell + " " + Utils.BREAK_LINE;
            if (!lastLineText.endsWith("$") && lastLineText.trim().length() != 0) {
                shell = "#" + Utils.BREAK_LINE + shell;
            }
            log.info("executeShell  getCurrentSession sendString");
            sendString(shell);
        }
    }

    /**
     * 执行shell
     *
     * @param shell
     */
    public void executeShell(String[] shell) {
        executeShell(shell[1]);
    }

    public void initTerminal(final ToolWindow toolWindow) {
        toolWindow.setToHideOnEmptyContent(true);
        LocalTerminalDirectRunner terminalRunner = LocalTerminalDirectRunner.createTerminalRunner(myProject);
        toolWindow.setStripeTitle("Wink");
        Content content = createTerminalInContentPanel(terminalRunner, toolWindow);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setShowStripeButton(true);
        toolWindow.setTitle("Console");
        ((ToolWindowManagerEx) ToolWindowManager.getInstance(this.myProject)).addToolWindowManagerListener(new ToolWindowManagerListener() {
            @Override
            public void toolWindowRegistered(@NotNull String s) {

            }

            @Override
            public void stateChanged() {
                ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(WinkToolWindowFactory.TOOL_WINDOW_ID);
                if (window != null) {
                    boolean visible = window.isVisible();
                    if (visible && toolWindow.getContentManager().getContentCount() == 0) {
                        initTerminal(window);
                    }
                }
            }
        });
        toolWindow.show(null);
        JBTerminalWidget terminalWidget = getTerminalWidget(toolWindow);
        if (terminalWidget != null && terminalWidget.getCurrentSession() != null) {
            Terminal terminal = terminalWidget.getCurrentSession().getTerminal();
            if (terminal != null) {
                terminal.setCursorVisible(false);
            }
        }
    }

    private ToolWindow getToolWindow() {
        return ToolWindowManager.getInstance(myProject).getToolWindow(WinkToolWindowFactory.TOOL_WINDOW_ID);
    }

    /**
     * 创建Terminal panel
     *
     * @param terminalRunner
     * @param toolWindow
     * @return
     */
    private Content createTerminalInContentPanel(@NotNull AbstractTerminalRunner terminalRunner, @NotNull final ToolWindow toolWindow) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
        content.setCloseable(true);
        myTerminalWidget = terminalRunner.createTerminalWidget(content, null);
        panel.setContent(myTerminalWidget.getComponent());
        panel.addFocusListener(this);
        ActionToolbar toolbar = createToolbar(terminalRunner, myTerminalWidget, toolWindow);
        toolbar.setTargetComponent(panel);
        panel.setToolbar(toolbar.getComponent());
        content.setPreferredFocusableComponent(myTerminalWidget.getComponent());
        return content;
    }

    /**
     * 创建左侧工具栏
     *
     * @param terminalRunner
     * @param terminal
     * @param toolWindow
     * @return
     */
    private ActionToolbar createToolbar(@Nullable AbstractTerminalRunner terminalRunner, @NotNull JBTerminalWidget terminal, @NotNull ToolWindow toolWindow) {
        DefaultActionGroup group = new DefaultActionGroup();
        if (terminalRunner != null) {
            group.add(new RunAction(this));
//            group.add(new StopAction(this));
//            group.addSeparator();
//            group.add(new DebugAction(this));
//            group.add(new ForceAction(this));
//            group.addSeparator();
//            group.add(new ClearAction(this));
        }
        return ActionManager.getInstance().createActionToolbar("unknown", group, false);
    }

    @Override
    public void focusGained(FocusEvent e) {
        JComponent component = myTerminalWidget != null ? myTerminalWidget.getComponent() : null;
        if (component != null) {
            component.requestFocusInWindow();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "FreelineTerminal";
    }

    /**
     * 停止执行
     */
    private static class StopAction extends BaseTerminalAction {
        private Robot robot;
        public StopAction(WinkTerminal terminal) {
            super(terminal, "Stop Run Freeline", "Stop Run Freeline", PluginIcons.Suspend);
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void doAction(AnActionEvent anActionEvent) {
            if (terminal.getCurrentSession() != null) {
                terminal.getCurrentSession().getComponent().requestFocusInWindow();
                Utils.keyPressWithCtrl(robot, KeyEvent.VK_C);
            }
        }
    }

    private static class RunAction extends BaseTerminalAction {
        String pythonLocation;
        public RunAction(WinkTerminal terminal) {
            this(terminal, ConstantPool.COMMAND, "Run Wink", PluginIcons.WinkIcon);
        }

        public RunAction(WinkTerminal terminal, String text, String description, Icon icon) {
            super(terminal, text, description, icon);
        }

        @Override
        public void doAction(AnActionEvent anActionEvent) {
            Project currentProject = anActionEvent.getProject();
            File shellFile = new File(currentProject.getBasePath()+File.separator+ConstantPool.IDEA_WINK_LIB_DIR, ConstantPool.WINK_SHELL);
            if (shellFile.exists() && shellFile.isFile()) {
                shellFile.setExecutable(true,false);
                WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_SHELL);
            }else {
                WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_INIT +" && "+ConstantPool.COMMAND_SHELL );
                //WinkTerminal.getInstance(currentProject).initAndExecute(ConstantPool.COMMAND_SHELL);
            }
        }

        private String[] getArgs() {
            return null;
        }

        protected String args() {
            return null;
        }
    }

    private static class DebugAction extends RunAction {
        public DebugAction(WinkTerminal terminal) {
            super(terminal, "Run Freeline -d", "Run Freeline -d", PluginIcons.StartDebugger);
        }

        @Override
        protected String args() {
            return "-d";
        }
    }

    private static class ForceAction extends RunAction {
        public ForceAction(WinkTerminal terminal) {
            super(terminal, "Run Freeline -f", "Run Freeline -f", PluginIcons.QuickfixBulb);
        }

        @Override
        protected String args() {
            return "-f";
        }
    }

    /**
     * 清空terminal
     */
    private static class ClearAction extends BaseTerminalAction {
        public ClearAction(WinkTerminal terminal) {
            super(terminal, "Clear", "Clear", PluginIcons.GC);
        }

        @Override
        public void doAction(AnActionEvent anActionEvent) {
            if (terminal.getCurrentSession() != null) {
                terminal.getCurrentSession().getTerminal().reset();
                terminal.getCurrentSession().getTerminal().setCursorVisible(false);
            }
        }
    }

    private static abstract class BaseTerminalAction extends DumbAwareAction {
        protected WinkTerminal terminal;

        public BaseTerminalAction(WinkTerminal terminal, String text, String description, Icon icon) {
            super(text, description, icon);
            this.terminal = terminal;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            DocumentUtil.saveDocument();
            //if (FreelineUtil.checkInstall(anActionEvent.getProject())) {
                doAction(anActionEvent);
           // }
        }

        public abstract void doAction(AnActionEvent anActionEvent);
    }
}
