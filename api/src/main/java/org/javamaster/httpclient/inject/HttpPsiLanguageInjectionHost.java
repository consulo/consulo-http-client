package org.javamaster.httpclient.inject;

import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.LeafElement;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

/**
 * Add element injection support
 *
 * @author yudong
 */
public class HttpPsiLanguageInjectionHost extends ASTWrapperPsiElement implements PsiLanguageInjectionHost {

    public HttpPsiLanguageInjectionHost(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @NotNull
    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        ASTNode valueNode = getNode().getFirstChildNode();
        boolean leafElement = valueNode instanceof LeafElement;

        assert leafElement;

        ((LeafElement) valueNode).replaceWithText(text);

        return this;
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new JSStringLiteralEscaper<PsiLanguageInjectionHost>(this) {
            @Override
            protected boolean isRegExpLiteral() {
                return false;
            }

            @Override
            public boolean isOneLine() {
                return false;
            }
        };
    }
}
