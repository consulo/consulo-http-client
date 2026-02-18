package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpFilePath;
import org.javamaster.httpclient.impl.reference.support.HttpFilePathPsiReference;
import org.jetbrains.annotations.NotNull;

public class HttpFilePathPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var textRange = element.getTextRange();
        var httpFilePath = (HttpFilePath) element;

        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new HttpFilePathPsiReference(httpFilePath, range)};
    }

}
