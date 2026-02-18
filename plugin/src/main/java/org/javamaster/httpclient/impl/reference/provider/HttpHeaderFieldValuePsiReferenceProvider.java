package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.*;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpHeaderFieldValue;
import org.javamaster.httpclient.impl.reference.support.HttpHeaderFieldValuePsiReference;
import org.jetbrains.annotations.NotNull;

public class HttpHeaderFieldValuePsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var textRange = element.getTextRange();
        var fieldValue = (HttpHeaderFieldValue) element;

        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new HttpHeaderFieldValuePsiReference(fieldValue, range)};
    }

}
