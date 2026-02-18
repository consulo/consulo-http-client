package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpRequestTarget;
import org.javamaster.httpclient.impl.reference.support.HttpUrlControllerMethodPsiReference;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpUrlControllerMethodPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var httpRequestTarget = (HttpRequestTarget) element;

        var virtualFile = HttpUtils.getOriginalFile(httpRequestTarget);
        if (virtualFile == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var parent = virtualFile.getParent();
        if (parent == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var path = parent.getPath();

        var pair = HttpUtils.getSearchTxtInfo(httpRequestTarget, path);
        if (pair == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var searchTxt = pair.getFirst();
        var textRange = pair.getSecond();

        return new PsiReference[]{new HttpUrlControllerMethodPsiReference(searchTxt, httpRequestTarget, textRange)};
    }

}
