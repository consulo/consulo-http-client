package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class TextVariableNamePsiReference extends PsiReferenceBase<PsiElement> {

    private final PsiElement psiElement;
    private final HttpVariable variable;
    private final TextRange textRange;
    private final HttpMessageBody messageBody;

    public TextVariableNamePsiReference(
        @NotNull PsiElement psiElement,
        @NotNull HttpVariable variable,
        TextRange textRange,
        @Nullable HttpMessageBody messageBody
    ) {
        super(psiElement, textRange.shiftLeft(psiElement.getTextRange().getStartOffset()), true);
        this.psiElement = psiElement;
        this.variable = variable;
        this.textRange = textRange;
        this.messageBody = messageBody;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    public HttpVariable getVariable() {
        return variable;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var element = messageBody != null ? messageBody : psiElement;

        var variableName = variable.getVariableName();
        if (variableName == null) {
            return null;
        }

        var name = variableName.getName();
        var builtin = variableName.isBuiltin();

        return HttpVariableNamePsiReference.tryResolveVariable(name, builtin, element, false);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return HttpVariableNamePsiReference.getVariableVariants(getElement());
    }
}
