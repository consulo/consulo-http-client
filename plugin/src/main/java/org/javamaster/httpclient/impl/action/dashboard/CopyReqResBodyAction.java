package org.javamaster.httpclient.impl.action.dashboard;

import consulo.ui.ex.action.AnActionEvent;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.ide.impl.idea.openapi.ide.CopyPasteManager;
import consulo.project.Project;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpBody;
import org.javamaster.httpclient.psi.HttpOutputFile;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class CopyReqResBodyAction extends DashboardBaseAction {
    public CopyReqResBodyAction() {
        super(NlsBundle.message("cy.body"), HttpIcons.COPY);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getHttpEditor(e);
        Project project = editor.getProject();
        Document document = editor.getDocument();
        var httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();

        JComponent component = (JComponent) PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());

        if (isReq(e)) {
            HttpBody httpBody = PsiTreeUtil.findChildOfType(httpFile, HttpBody.class);
            if (httpBody == null) return;
            String text = httpBody.getText();

            copyPasteManager.setContents(new StringSelection(text));
        } else {
            SimpleTypeEnum simpleTypeEnum = component.getUserData(httpDashboardResTypeKey);
            if (simpleTypeEnum == null) return;

            if (simpleTypeEnum.isBinary()) {
                HttpOutputFile outputFile = PsiTreeUtil.findChildOfType(httpFile, HttpOutputFile.class);

                copyPasteManager.setContents(new StringSelection(outputFile.getFilePath().getText()));

                return;
            }

            HttpBody httpBody = PsiTreeUtil.findChildOfType(httpFile, HttpBody.class);
            if (httpBody == null) return;
            String text = httpBody.getText();

            copyPasteManager.setContents(new StringSelection(text));
        }
    }
}
