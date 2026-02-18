package org.javamaster.httpclient.impl.reference;

import consulo.language.pattern.PlatformPatterns;
import consulo.language.plain.psi.PsiPlainTextFile;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class TextPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiPlainTextFile.class), new TextPsiReferenceProvider()
        );
    }

}
