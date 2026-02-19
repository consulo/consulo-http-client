package org.javamaster.httpclient.impl.intent;

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseElementAtCaretIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.httpClient.localize.HttpClientLocalize;
import org.javamaster.httpclient.impl.action.ConvertToCurlAndCpAction;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class ConvertToCurlAndCpIntention extends BaseElementAtCaretIntentionAction {

    @NotNull
    @Override
    public LocalizeValue getText() {
        return HttpClientLocalize.convertToCurlCp();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        return PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock.class) != null;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        HttpRequestBlock requestBlock = PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock.class);
        if (requestBlock == null) {
            return;
        }

        ConvertToCurlAndCpAction.convertToCurlAnCy(requestBlock, project, editor);
    }
}
