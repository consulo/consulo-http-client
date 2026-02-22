package org.javamaster.httpclient.impl.utils;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.codeEditor.EditorSettings;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.impl.action.dashboard.DashboardBaseAction;
import org.javamaster.httpclient.impl.action.dashboard.ShowLineNumberAction;
import org.javamaster.httpclient.impl.action.dashboard.SoftWrapAction;
import org.javamaster.httpclient.model.SimpleTypeEnum;

import java.io.IOException;
import java.util.List;

public class HttpUiUtils {

    public static Editor createEditor(
        byte[] bytes,
        String suffix,
        Project project,
        String tabName,
        List<Editor> editorList,
        boolean req,
        SimpleTypeEnum simpleTypeEnum,
        boolean noLog
    ) {
        Editor editor = createEditor(bytes, suffix, project, tabName, editorList, noLog);
        var document = editor.getDocument();

        EditorSettings settings = editor.getSettings();
        if (req) {
            settings.setUseSoftWraps(SoftWrapAction.reqUseSoftWrap);
            settings.setLineNumbersShown(ShowLineNumberAction.reqShowLineNum);

            // TODO FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.reqFoldHeader, editor);
            // TODO ((UserDataHolderEx) document).putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.reqFoldHeader);
        }
        else {
            settings.setUseSoftWraps(SoftWrapAction.resUseSoftWrap);
            settings.setLineNumbersShown(ShowLineNumberAction.resShowLineNum);

            // TODO FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.resFoldHeader, editor);
            // TODO ((UserDataHolderEx) document).putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.resFoldHeader);
        }

        var component = editor.getComponent();

        component.putClientProperty(DashboardBaseAction.httpDashboardToolbarKey, req);
        component.putClientProperty(DashboardBaseAction.httpDashboardResTypeKey, simpleTypeEnum);

        var key = req
            ? DashboardBaseAction.httpDashboardReqEditorKey
            : DashboardBaseAction.httpDashboardResEditorKey;

        component.putClientProperty(key, editor);

        return editor;
    }

    public static Editor createEditor(
        byte[] bytes,
        String suffix,
        Project project,
        String tabName,
        List<Editor> editorList,
        boolean noLog
    ) {
        VirtualFile virtualFile = null;
        try {
            virtualFile = VirtualFileUtils.createHttpVirtualFileFromText(bytes, suffix, project, tabName, noLog);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile psiFile = PsiUtilCore.getPsiFile(project, virtualFile);

        var document = psiDocumentManager.getDocument(psiFile);

        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(document, project, virtualFile, true);
        editorList.add(editor);

        return editor;
    }
}
