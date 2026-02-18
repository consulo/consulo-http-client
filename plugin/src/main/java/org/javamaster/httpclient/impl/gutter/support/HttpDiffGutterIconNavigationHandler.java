package org.javamaster.httpclient.impl.gutter.support;

import consulo.codeEditor.Editor;
import consulo.diff.DiffContentFactory;
import consulo.diff.DiffManager;
import consulo.diff.DiffRequestFactory;
import consulo.diff.content.DiffContent;
import consulo.diff.request.SimpleDiffRequest;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.hint.HintManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.ex.awt.JBLabel;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpHistoryBodyFile;
import org.javamaster.httpclient.psi.HttpHistoryBodyFileList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
public class HttpDiffGutterIconNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
    public static final HttpDiffGutterIconNavigationHandler INSTANCE = new HttpDiffGutterIconNavigationHandler();

    private HttpDiffGutterIconNavigationHandler() {
    }

    @Override
    public void navigate(@NotNull MouseEvent event, @NotNull PsiElement element) {
        Project project = element.getProject();
        VirtualFile editorVirtualFile = PsiUtilCore.getVirtualFile(element);
        if (editorVirtualFile == null) {
            return;
        }

        HttpHistoryBodyFile currentBodyFile = (HttpHistoryBodyFile) element.getParent();
        if (currentBodyFile.getFilePath() == null) {
            return;
        }

        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        HintManager hintManager = HintManager.getInstance();

        Editor editor = editorManager.getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        String historyPath = editorVirtualFile.getParent().getPath();

        File currentFile = new File(historyPath, currentBodyFile.getFilePath().getText());
        VirtualFile currentVirtualFile = VirtualFileUtil.findFileByIoFile(currentFile, true);

        if (currentVirtualFile == null) {
            editor.getCaretModel().moveToOffset(element.getTextRange().getEndOffset());
            hintManager.showErrorHint(editor, NlsBundle.message("file.not.exists", currentFile.getName()));
            return;
        }

        Map<String, Pair> map = new LinkedHashMap<>();

        HttpHistoryBodyFileList historyBodyFileList = (HttpHistoryBodyFileList) currentBodyFile.getParent();

        historyBodyFileList.getHistoryBodyFileList().stream()
                .filter(it -> it != currentBodyFile && it.getFilePath() != null)
                .forEach(it -> {
                    File file = new File(historyPath, it.getFilePath().getText());
                    map.put(file.getName(), new Pair(it, file));
                });

        List<String> keys = map.keySet().stream().collect(Collectors.toList());

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(keys)
                .setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                    String text = "    " + NlsBundle.message("compare.with") + " " + value + "    ";
                    return new JBLabel(text, PlatformIconGroup.actionsDiff(), SwingConstants.CENTER);
                })
                .setItemChosenCallback(it -> {
                    Pair pair = map.get(it);
                    File chooseFile = pair.second;

                    VirtualFile chooseVirtualFile = VirtualFileUtil.findFileByIoFile(chooseFile, true);
                    if (chooseVirtualFile == null) {
                        editor.getCaretModel().moveToOffset(pair.first.getTextRange().getEndOffset());
                        hintManager.showErrorHint(editor, NlsBundle.message("file.not.exists", chooseFile.getName()));
                        return;
                    }

                    DiffContentFactory contentFactory = DiffContentFactory.getInstance();
                    DiffRequestFactory requestFactory = DiffRequestFactory.getInstance();

                    DiffContent content1 = contentFactory.create(project, currentVirtualFile);
                    DiffContent content2 = contentFactory.create(project, chooseVirtualFile);

                    String title = requestFactory.getTitle(currentVirtualFile);

                    SimpleDiffRequest request = new SimpleDiffRequest(title, content1, content2, null, null);

                    DiffManager.getInstance().showDiff(project, request);
                })
                .createPopup()
                .showInScreenCoordinates(event.getComponent(), event.getLocationOnScreen());
    }

    private static class Pair {
        final HttpHistoryBodyFile first;
        final File second;

        Pair(HttpHistoryBodyFile first, File second) {
            this.first = first;
            this.second = second;
        }
    }
}
