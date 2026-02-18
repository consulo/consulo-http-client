package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpVariableArg;
import org.javamaster.httpclient.impl.reference.support.HttpVariableArgPsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpVariableArgPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context
    ) {
        var variableArg = (HttpVariableArg) element;

        var textRange = variableArg.getTextRange();
        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new HttpVariableArgPsiReference(variableArg, range)};
    }

}
