package org.javamaster.httpclient.impl.reference;

import com.intellij.json.psi.JsonStringLiteral;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.impl.reference.provider.JsonValuePsiReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Json里的所有变量声明都已脱离出来单独注入为 PlainText 类型,所以这个 Contributor 不再需要
 *
 * @author yudong
 */
public class JsonValuePsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral.class),
            new JsonValuePsiReferenceProvider()
        );
    }

}
