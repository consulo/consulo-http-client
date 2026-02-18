package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class QueryValuePsiReference extends PsiReferenceBase<PsiElement> {

    private final TextRange textRange;

    public QueryValuePsiReference(@NotNull PsiElement psiElement, TextRange textRange) {
        super(psiElement, textRange);
        this.textRange = textRange;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

}
