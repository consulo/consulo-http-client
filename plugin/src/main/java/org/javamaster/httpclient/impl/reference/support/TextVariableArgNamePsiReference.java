package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpVariableArg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class TextVariableArgNamePsiReference extends PsiReferenceBase<PsiElement> {

    private final PsiElement psiElement;
    private final HttpVariableArg variableArg;
    private final TextRange textRange;
    private final HttpMessageBody messageBody;

    public TextVariableArgNamePsiReference(
        @NotNull PsiElement psiElement,
        @NotNull HttpVariableArg variableArg,
        TextRange textRange,
        @Nullable HttpMessageBody messageBody
    ) {
        super(psiElement, textRange.shiftLeft(psiElement.getTextRange().getStartOffset()), true);
        this.psiElement = psiElement;
        this.variableArg = variableArg;
        this.textRange = textRange;
        this.messageBody = messageBody;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var element = messageBody != null ? messageBody : psiElement;

        var containingFile = element.getContainingFile();
        var virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        var parent = virtualFile.getParent();
        if (parent == null) {
            return null;
        }

        var httpFileParentPath = parent.getPath();

        var guessPath = variableArg.getValue().toString();

        return HttpVariableArgPsiReference.tryResolvePath(guessPath, httpFileParentPath, getElement().getProject());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

}
