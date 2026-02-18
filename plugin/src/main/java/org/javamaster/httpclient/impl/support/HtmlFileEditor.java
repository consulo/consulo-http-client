package org.javamaster.httpclient.impl.support;

import consulo.xml.lang.html.HTMLLanguage;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorState;
import consulo.project.Project;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.light.LightVirtualFile;
import consulo.ui.ex.jcef.JCEFHtmlPanel;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.ui.HtmlPreviewForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
public class HtmlFileEditor extends UserDataHolderBase implements FileEditor {
    private final HtmlPreviewForm htmlPreviewForm = new HtmlPreviewForm();
    private final JCEFHtmlPanel jcefHtmlPanel;
    private final Editor textEditor;
    private final VirtualFile resBodyFile;

    public HtmlFileEditor(Project project, VirtualFile resBodyFile) {
        this.resBodyFile = resBodyFile;
        this.jcefHtmlPanel = new JCEFHtmlPanel(null);

        String extension = resBodyFile.getExtension();
        boolean pdf = extension != null && extension.equalsIgnoreCase(SimpleTypeEnum.PDF.getType());

        String html;
        if (pdf) {
            html = "<html>\n" +
                    "<body style=\"margin: 0\">\n" +
                    "    <div>\n" +
                    "      <object data=\"file:///" + resBodyFile.getPath() + "\" type=\"application/pdf\" width=\"100%\" height=\"100%\"></object>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";
        } else {
            try {
                html = new String(resBodyFile.contentsToByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                html = "";
            }
        }

        jcefHtmlPanel.setHtml(html);

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);

        PsiFile psiFile = PsiUtil.getPsiFile(project, new LightVirtualFile("res.html", HtmlFileType.INSTANCE, html));
        var document = psiDocumentManager.getDocument(psiFile);
        if (document == null) {
            throw new IllegalStateException("Document is null");
        }

        EditorFactory editorFactory = EditorFactory.getInstance();
        textEditor = editorFactory.createEditor(document, project, HtmlFileType.INSTANCE, true);

        htmlPreviewForm.initTabs(jcefHtmlPanel.getComponent(), textEditor.getComponent(), pdf);
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return resBodyFile;
    }

    @Override
    public void dispose() {
        jcefHtmlPanel.dispose();
        EditorFactory.getInstance().releaseEditor(textEditor);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        htmlPreviewForm.getTabbedPane().setSelectedIndex(0);
        return htmlPreviewForm.getMainPanel();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        htmlPreviewForm.getTabbedPane().setSelectedIndex(0);
        return htmlPreviewForm.getMainPanel();
    }

    @NotNull
    @Override
    public String getName() {
        return "HtmlFileEditor";
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }
}
