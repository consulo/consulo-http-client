package org.javamaster.httpclient.impl.reference.provider;

import consulo.json.psi.JsonStringLiteral;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiMethod;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.impl.reference.support.JsonKeyControllerMethodFieldPsiReference;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class JsonKeyControllerMethodFieldPsiReferenceProvider extends PsiReferenceProvider {

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

        var requestTarget = httpRequest.getRequestTarget();
        if (requestTarget == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var references = requestTarget.getReferences();
        if (references.length == 0) {
            return PsiReference.EMPTY_ARRAY;
        }

        var resolved = references[0].resolve();
        if (!(resolved instanceof PsiMethod)) {
            return PsiReference.EMPTY_ARRAY;
        }

        var controllerMethod = (PsiMethod) resolved;

        var textRange = jsonString.getTextRange();
        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new JsonKeyControllerMethodFieldPsiReference(jsonString, controllerMethod, range)};
    }

}
