package org.javamaster.httpclient.impl.reference;

import consulo.json.psi.JsonStringLiteral;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.impl.reference.provider.JsonKeyControllerMethodFieldPsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Jump to the SpringMVC Controller method param Bean field when pressing Ctrl + click json key
 *
 * @author yudong
 */
public class JsonKeyControllerMethodFieldPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral.class),
            new JsonKeyControllerMethodFieldPsiReferenceProvider()
        );
    }

}
