package org.javamaster.httpclient.impl.action.example;

import consulo.ide.impl.idea.ide.impl.ProjectUtil;
import consulo.ui.ex.action.ActionUpdateThread;
import consulo.ui.ex.action.AnAction;
import consulo.fileEditor.FileEditorManager;
import consulo.project.Project;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.VirtualFile;

import java.net.URL;

/**
 * @author yudong
 */
public abstract class ExampleAction extends AnAction {

    public ExampleAction(String text) {
        super(text);
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    protected void openExample(String name) {
        Project project = ProjectUtil.getActiveProject();
        URL url = getClass().getClassLoader().getResource(name);
        VirtualFile virtualFile = VfsUtil.findFileByURL(url);
        FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }
}
