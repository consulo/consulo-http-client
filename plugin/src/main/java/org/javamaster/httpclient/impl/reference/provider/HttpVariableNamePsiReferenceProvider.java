package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpVariableName;
import org.javamaster.httpclient.impl.reference.support.HttpVariableNamePsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpVariableNamePsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context
    ) {
        var variable = (HttpVariableName) element;

        var textRange = variable.getTextRange();
        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new HttpVariableNamePsiReference(variable, range)};
    }

}
