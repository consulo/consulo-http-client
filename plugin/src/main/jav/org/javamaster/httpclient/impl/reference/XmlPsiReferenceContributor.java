package org.javamaster.httpclient.impl.reference;

import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.impl.reference.provider.XmlTagPsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class XmlPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            StandardPatterns.or(
                PlatformPatterns.psiElement(XmlToken.class),
                PlatformPatterns.psiElement(XmlAttributeValue.class)
            ),
            new XmlTagPsiReferenceProvider()
        );
    }

}
