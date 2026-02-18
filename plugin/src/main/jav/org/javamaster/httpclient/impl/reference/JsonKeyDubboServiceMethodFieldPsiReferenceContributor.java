package org.javamaster.httpclient.impl.reference;

import consulo.json.psi.JsonStringLiteral;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.impl.reference.provider.JsonKeyDubboServiceMethodFieldPsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Jump to the Dubbo service method param field when pressing Ctrl + click json key
 *
 * @author yudong
 */
public class JsonKeyDubboServiceMethodFieldPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral.class),
            new JsonKeyDubboServiceMethodFieldPsiReferenceProvider()
        );
    }

}
