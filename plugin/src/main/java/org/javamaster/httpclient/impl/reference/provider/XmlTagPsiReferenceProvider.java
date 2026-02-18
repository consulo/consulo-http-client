package org.javamaster.httpclient.impl.reference.provider;

import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class XmlTagPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context
    ) {
        var injectionHost = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
        if (!(injectionHost instanceof HttpMessageBody)) {
            return PsiReference.EMPTY_ARRAY;
        }

        var text = element.getText();
        var delta = element.getTextRange().getStartOffset();

        return TextPsiReferenceProvider.createTextVariableReferences(element, (HttpMessageBody) injectionHost, text, delta);
    }

}
