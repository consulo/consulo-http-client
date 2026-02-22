package org.javamaster.httpclient.impl.action.dashboard;

import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.virtualFileSystem.VirtualFile;

/**
 * @author yudong
 */
public class PreviewFileAction extends DashboardBaseAction {
    private final VirtualFile resBodyFile;

    public PreviewFileAction(VirtualFile resBodyFile) {
        super(HttpClientLocalize.preview(), PlatformIconGroup.actionsPreview());
        this.resBodyFile = resBodyFile;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getHttpEditor(e);
        Project project = editor.getProject();

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(resBodyFile, true);
    }
}
