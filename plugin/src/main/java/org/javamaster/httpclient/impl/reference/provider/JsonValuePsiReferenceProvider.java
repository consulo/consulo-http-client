package org.javamaster.httpclient.impl.reference.provider;

import com.intellij.json.psi.JsonStringLiteral;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class JsonValuePsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context
    ) {
        var stringLiteral = (JsonStringLiteral) element;

        if (stringLiteral.isPropertyName()) {
            return PsiReference.EMPTY_ARRAY;
        }

        var project = stringLiteral.getProject();

        var injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(stringLiteral);
        if (injectionHost instanceof HttpMessageBody) {
            var text = stringLiteral.getText();
            if (text.length() < 4) {
                return PsiReference.EMPTY_ARRAY;
            }

            text = text.substring(1, text.length() - 1);
            var delta = stringLiteral.getTextRange().getStartOffset() + 1;

            return TextPsiReferenceProvider.createTextVariableReferences(stringLiteral, (HttpMessageBody) injectionHost, text, delta);
        }

        var containingFile = element.getContainingFile();
        var virtualFile = containingFile != null ? containingFile.getVirtualFile() : null;
        var fileName = virtualFile != null ? virtualFile.getName() : null;

        if (EnvFileService.ENV_FILE_NAMES.contains(fileName)) {
            var text = stringLiteral.getText();
            text = text.substring(1, text.length() - 1);
            var delta = stringLiteral.getTextRange().getStartOffset() + 1;

            return TextPsiReferenceProvider.createTextVariableReferences(stringLiteral, null, text, delta);
        }

        return PsiReference.EMPTY_ARRAY;
    }

}
