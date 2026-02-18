package org.javamaster.httpclient.impl.reference.provider;

import consulo.json.psi.JsonStringLiteral;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.impl.reference.support.JsonKeyDubboMethodFieldPsiReference;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class JsonKeyDubboServiceMethodFieldPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var jsonString = (JsonStringLiteral) element;
        var project = jsonString.getProject();

        var messageBody = HttpUtils.getInjectHost(jsonString, project);
        if (messageBody == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest.class);
        if (httpRequest == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var method = httpRequest.getMethod();
        if (!method.getText().equals(HttpRequestEnum.DUBBO.name())) {
            return PsiReference.EMPTY_ARRAY;
        }

        var textRange = jsonString.getTextRange();
        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new JsonKeyDubboMethodFieldPsiReference(jsonString, range)};
    }

}
