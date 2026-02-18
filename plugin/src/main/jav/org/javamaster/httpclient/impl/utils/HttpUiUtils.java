package org.javamaster.httpclient.impl.utils;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.codeEditor.EditorSettings;
import consulo.project.Project;
import consulo.util.dataholder.UserDataHolderEx;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.impl.action.dashboard.DashboardBaseAction;
import org.javamaster.httpclient.impl.action.dashboard.SoftWrapAction;
import org.javamaster.httpclient.impl.action.dashboard.view.FoldHeadersAction;
import org.javamaster.httpclient.impl.action.dashboard.view.ShowLineNumberAction;
import org.javamaster.httpclient.model.SimpleTypeEnum;

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

            FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.reqFoldHeader, editor);
            ((UserDataHolderEx) document).putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.reqFoldHeader);
        } else {
            settings.setUseSoftWraps(SoftWrapAction.resUseSoftWrap);
            settings.setLineNumbersShown(ShowLineNumberAction.resShowLineNum);

            FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.resFoldHeader, editor);
            ((UserDataHolderEx) document).putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.resFoldHeader);
        }

        var component = editor.getComponent();

        ((UserDataHolderEx) component).putUserData(DashboardBaseAction.httpDashboardToolbarKey, req);
        ((UserDataHolderEx) component).putUserData(DashboardBaseAction.httpDashboardResTypeKey, simpleTypeEnum);

        var key = req
            ? DashboardBaseAction.httpDashboardReqEditorKey
            : DashboardBaseAction.httpDashboardResEditorKey;

        ((UserDataHolderEx) component).putUserData(key, editor);

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
        VirtualFile virtualFile = VirtualFileUtils.createHttpVirtualFileFromText(bytes, suffix, project, tabName, noLog);

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile psiFile = PsiUtil.getPsiFile(project, virtualFile);

        var document = psiDocumentManager.getDocument(psiFile);

        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(document, project, virtualFile, true);
        editorList.add(editor);

        return editor;
    }
}
