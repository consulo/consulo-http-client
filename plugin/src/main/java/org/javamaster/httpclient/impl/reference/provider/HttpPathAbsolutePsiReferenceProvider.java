package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpPathAbsolute;
import org.javamaster.httpclient.impl.reference.support.HttpPathAbsolutePsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpPathAbsolutePsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var httpPathAbsolute = (HttpPathAbsolute) element;

        var textRange = httpPathAbsolute.getTextRange();
        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new HttpPathAbsolutePsiReference(httpPathAbsolute, range)};
    }

}
