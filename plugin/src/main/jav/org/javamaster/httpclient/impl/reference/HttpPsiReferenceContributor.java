package org.javamaster.httpclient.impl.reference;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.impl.reference.provider.*;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpVariableName.class), new HttpVariableNamePsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpVariableArg.class), new HttpVariableArgPsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpFilePath.class), new HttpFilePathPsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpHeaderFieldValue.class), new HttpHeaderFieldValuePsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpDirectionValue.class).withSuperParent(2, HttpFile.class),
            new HttpDirectionValuePsiReferenceProvider()
        );

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpQueryParameterKey.class), new HttpQueryParameterKeyPsiReferenceProvider()
        );
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
