package org.javamaster.httpclient.impl.action.dashboard.view;

import consulo.language.editor.folding.FoldingUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.CustomComponentAction;
import consulo.ui.ex.action.ActionButtonWithText;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.codeEditor.FoldRegion;
import consulo.codeEditor.FoldingModel;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.action.dashboard.DashboardBaseAction;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.folding.HttpFoldingBuilder;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpHeader;
import org.javamaster.httpclient.psi.HttpRequestBlock;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author yudong
 */
public class FoldHeadersAction extends DashboardBaseAction implements CustomComponentAction {
    public static final Key<Boolean> httpDashboardFoldHeaderKey = HttpFoldingBuilder.httpDashboardFoldHeaderKey;
    public static boolean reqFoldHeader = true;
    public static boolean resFoldHeader = true;

    private final Editor editor;
    private final boolean req;

    public FoldHeadersAction(Editor editor, boolean req) {
        super(NlsBundle.message("fold.headers.default"), null);
        this.editor = editor;
        this.req = req;
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        ActionButtonWithText actionButtonWithText = new ActionButtonWithText(this, presentation, place, new Dimension(20, 20));

        boolean foldHeader = getFoldHeader();

        setFoldHeader(foldHeader, actionButtonWithText);

        return actionButtonWithText;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionButtonWithText actionButtonWithText = (ActionButtonWithText) e.getInputEvent().getComponent();

        boolean foldHeader = switchFoldHeader();

        setFoldHeader(foldHeader, actionButtonWithText);
    }

    private void setFoldHeader(boolean foldHeader, ActionButtonWithText actionButtonWithText) {
        JComponent component = editor.getComponent();
        if (component.getUserData(httpDashboardResTypeKey) == null) return;

        setEditorFoldHeader(foldHeader, editor);

        if (foldHeader) {
            actionButtonWithText.getPresentation().setIcon(PlatformIconGroup.actionsChecked);
        } else {
            actionButtonWithText.getPresentation().setIcon(HttpIcons.BLANK);
        }
    }

    private boolean getFoldHeader() {
        if (req) {
            return reqFoldHeader;
        } else {
            return resFoldHeader;
        }
    }

    private boolean switchFoldHeader() {
        if (req) {
            reqFoldHeader = !reqFoldHeader;
            return reqFoldHeader;
        } else {
            resFoldHeader = !resFoldHeader;
            return resFoldHeader;
        }
    }

    public static void setEditorFoldHeader(boolean foldHeader, Editor editor) {
        Project project = editor.getProject();
        Document document = editor.getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (!(psiFile instanceof HttpFile)) {
            return;
        }

        HttpFile httpFile = (HttpFile) psiFile;
        List<HttpRequestBlock> requestBlocks = httpFile.getRequestBlocks();
        if (requestBlocks.isEmpty()) {
            return;
        }

        HttpHeader header = requestBlocks.get(0).getRequest().getHeader();
        if (header == null) return;

        FoldingModel foldingModel = editor.getFoldingModel();

        List<FoldRegion> foldRegions = FoldingUtil.getFoldRegionsAtOffset(editor, header.getTextRange().getStartOffset());
        if (foldRegions.isEmpty()) {
            return;
        }

        foldingModel.runBatchFoldingOperation(() -> {
            foldRegions.get(0).setExpanded(!foldHeader);
        });
    }
}
