package org.javamaster.httpclient.impl.factory;

import consulo.language.ast.ASTFactory;
import consulo.language.impl.ast.Factory;
import consulo.application.Application;
import consulo.language.impl.psi.LeafElement;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.ast.IElementType;
import org.javamaster.httpclient.psi.HttpTypes;

public class HttpASTFactory extends ASTFactory {
    private final DefaultASTFactory myDefaultASTFactory =
            Application.get().getService(DefaultASTFactory.class);

    @Override
    public LeafElement createLeaf(IElementType type, CharSequence text) {
        if (type == HttpTypes.LINE_COMMENT) {
            return myDefaultASTFactory.createComment(type, text);
        }

        return new LeafPsiElement(type, text);
    }
}
