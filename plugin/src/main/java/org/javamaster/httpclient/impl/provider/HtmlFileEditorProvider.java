package org.javamaster.httpclient.impl.provider;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.impl.key.HttpKey;
import org.javamaster.httpclient.impl.support.HtmlFileEditor;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
@ExtensionImpl
public class HtmlFileEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Boolean value = virtualFile.getUserData(HttpKey.httpDashboardBinaryBodyKey);
        return value != null && value;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new HtmlFileEditor(project, virtualFile);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "htmlEditorTypeId";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
