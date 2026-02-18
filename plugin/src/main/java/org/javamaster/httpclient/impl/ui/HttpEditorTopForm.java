package org.javamaster.httpclient.impl.ui;

import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.project.Project;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.util.collection.ArrayUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.Trinity;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.impl.action.ChooseEnvironmentAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Set;

/**
 * @author yudong
 */
public class HttpEditorTopForm extends JComponent {
    public static final Key<HttpEditorTopForm> KEY = Key.create("httpRequest.httpEditorTopForm");

    public final VirtualFile file;

    public JPanel mainPanel;

    private JPanel btnLeftPanel;
    private JPanel btnRightPanel;

    private final ChooseEnvironmentAction chooseEnvironmentAction;

    private final @Nullable Module module;

    public HttpEditorTopForm(VirtualFile file, @Nullable Module module, FileEditor fileEditor) {
        this.file = file;
        this.module = module;

        ActionManager actionManager = ActionManager.getInstance();

        chooseEnvironmentAction = new ChooseEnvironmentAction(file);

        ActionGroup toolbarLeftBtnGroup = (ActionGroup) actionManager.getAction("httpToolbarLeftBtnGroup");
        assert toolbarLeftBtnGroup != null;

        DefaultActionGroup leftGroup = new DefaultActionGroup();
        leftGroup.addAll(toolbarLeftBtnGroup);
        leftGroup.addSeparator();
        leftGroup.add(chooseEnvironmentAction);

        ActionToolbar toolbarLeft = actionManager.createActionToolbar("httpRequestLeftToolbar", leftGroup, true);

        toolbarLeft.setTargetComponent(fileEditor.getComponent());

        btnLeftPanel.add(toolbarLeft.getComponent(), BorderLayout.CENTER);

        ActionGroup toolbarRightBtnGroup = (ActionGroup) actionManager.getAction("httpToolbarRightBtnGroup");
        assert toolbarRightBtnGroup != null;

        ActionToolbar toolbarRight = actionManager.createActionToolbar("HttpRequestRightToolbar", toolbarRightBtnGroup, true);

        toolbarRight.setTargetComponent(fileEditor.getComponent());

        btnRightPanel.add(toolbarRight.getComponent(), BorderLayout.CENTER);
    }

    public VirtualFile getFile() {
        return file;
    }

    public void initEnvCombo(Set<String> presetEnvSet) {
        if (presetEnvSet.contains("uat")) {
            setSelectEnv("uat");
        }
        else if (presetEnvSet.contains("test")) {
            setSelectEnv("test");
        }
    }

    public @Nullable String getSelectedEnv() {
        return chooseEnvironmentAction.getSelectedEnv();
    }

    public void setSelectEnv(String env) {
        chooseEnvironmentAction.setSelectEnv(env);
    }

    public static @Nullable String getSelectedEnv(Project project) {
        HttpEditorTopForm httpEditorTopForm = getSelectedEditorTopForm(project);
        if (httpEditorTopForm == null) {
            return null;
        }

        return httpEditorTopForm.getSelectedEnv();
    }

    public static @Nullable Trinity<String, VirtualFile, Module> getTriple(Project project) {
        HttpEditorTopForm topForm = getSelectedEditorTopForm(project);
        if (topForm == null) {
            return null;
        }

        return new Trinity<>(topForm.getSelectedEnv(), topForm.file, topForm.module);
    }

    public static @Nullable HttpEditorTopForm getSelectedEditorTopForm(Project project) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = ArrayUtil.getFirstElement(editorManager.getSelectedEditor());
        if (selectedEditor == null) {
            return null;
        }

        return selectedEditor.getUserData(HttpEditorTopForm.KEY);
    }

    public static void setCurrentEditorSelectedEnv(String httpFilePath, Project project, String env) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = ArrayUtil.getFirstElement(editorManager.getSelectedEditors());
        if (selectedEditor == null) {
            return;
        }

        VirtualFile virtualFile = selectedEditor.getFile();
        if (virtualFile == null || !Objects.equals(httpFilePath, virtualFile.getPath())) {
            return;
        }

        HttpEditorTopForm httpEditorTopForm = selectedEditor.getUserData(HttpEditorTopForm.KEY);
        if (httpEditorTopForm == null) {
            return;
        }

        httpEditorTopForm.setSelectEnv(env);
    }
}
