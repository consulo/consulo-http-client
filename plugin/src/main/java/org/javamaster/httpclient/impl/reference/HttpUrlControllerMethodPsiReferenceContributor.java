package org.javamaster.httpclient.impl.reference;

import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.psi.HttpPathAbsolute;
import org.javamaster.httpclient.psi.HttpRequestTarget;
import org.javamaster.httpclient.impl.reference.provider.HttpPathAbsolutePsiReferenceProvider;
import org.javamaster.httpclient.impl.reference.provider.HttpUrlControllerMethodPsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Jump to the Spring Controller method when pressing Ctrl + click url
 *
 * @author yudong
 */
public class HttpUrlControllerMethodPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpRequestTarget.class),
            new HttpUrlControllerMethodPsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpPathAbsolute.class),
            new HttpPathAbsolutePsiReferenceProvider()
        );
    }

}
