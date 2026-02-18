package org.javamaster.httpclient.impl.inspection.fix;

import consulo.application.Application;
import consulo.codeEditor.CaretModel;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.PriorityAction;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.parser.HttpFile;

/**
 * @author yudong
 */
public class CreateFileVariableQuickFix implements LocalQuickFix, PriorityAction {

    private final String variableName;

    public CreateFileVariableQuickFix(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getName() {
        return NlsBundle.message("unsolved.global.variable");
    }

    @Override
    public void applyFix(Project project, ProblemDescriptor descriptor) {
        createGlobalVariable(project);
    }

    private void createGlobalVariable(Project project) {
        if (!Application.get().isDispatchThread()) {
            return;
        }

        PsiElement elementCopy = HttpUtils.createGlobalVariableAndInsert(variableName, "", project);
        if (elementCopy == null) {
            return;
        }

        ((Navigatable) elementCopy.getLastChild()).navigate(true);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        Editor textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (textEditor == null) {
            return;
        }
        HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = documentManager.getDocument(httpFile);
        if (document == null) {
            return;
        }

        documentManager.doPostponedOperationsAndUnblockDocument(document);

        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();

        // Move the cursor to the value
        document.insertString(offset, " ");
        caretModel.moveToOffset(offset + 1);
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }
}
