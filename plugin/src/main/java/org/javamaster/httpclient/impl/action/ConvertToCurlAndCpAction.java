package org.javamaster.httpclient.impl.action;

import consulo.codeEditor.hint.HintManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.ActionUpdateThread;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.codeEditor.Editor;
import consulo.ide.impl.idea.openapi.ide.CopyPasteManager;
import consulo.project.Project;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.impl.curl.CurlParser;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;

import java.awt.datatransfer.StringSelection;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ConvertToCurlAndCpAction extends AnAction {
    public ConvertToCurlAndCpAction() {
        super(NlsBundle.message("convert.to.curl.cp"), null, PlatformIconGroup.generalInlineCopy);
    }

    @Override
    public void update(AnActionEvent e) {
        HttpRequestBlock requestBlock = findRequestBlock(e);

        e.getPresentation().setEnabled(requestBlock != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(Editor.KEY);
        if (editor == null) return;

        HttpRequestBlock requestBlock = findRequestBlock(e);
        if (requestBlock == null) return;

        Project project = e.getProject();

        convertToCurlAnCy(requestBlock, project, editor);
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public static HttpRequestBlock findRequestBlock(AnActionEvent e) {
        Editor editor = e.getData(Editor.KEY);
        if (editor == null) return null;

        var httpFile = PsiUtil.getPsiFile(e.getProject(), editor.getVirtualFile());

        var psiElement = httpFile.findElementAt(editor.getCaretModel().getOffset());
        if (psiElement == null) return null;

        return PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock.class);
    }

    public static void convertToCurlAnCy(HttpRequestBlock requestBlock, Project project, Editor editor) {
        String method = requestBlock.getRequest().getMethod().getText();
        if (method.equals(HttpRequestEnum.WEBSOCKET.name())
                || method.equals(HttpRequestEnum.DUBBO.name())) {
            NotifyUtil.notifyWarn(project, NlsBundle.message("convert.not.supported"));
            return;
        }

        try {
            CurlParser.toCurlString(requestBlock, project, false, it -> {
                CopyPasteManager.getInstance().setContents(new StringSelection(it));

                String str;
                if (it.length() > 2000) {
                    str = it.substring(0, 2000) + HttpUtils.CR_LF + "......";
                } else {
                    str = it;
                }

                HintManager.getInstance().showInformationHint(editor, NlsBundle.message("converted.tip") + "\n" + str);
            });
        } catch (Exception e) {
            NotifyUtil.notifyError(project, e.toString());
        }
    }
}
