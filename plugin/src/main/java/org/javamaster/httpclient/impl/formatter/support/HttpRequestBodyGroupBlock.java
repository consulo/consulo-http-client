package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.ast.IElementType;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpRequestBodyGroupBlock extends HttpRequestBaseBlock {

    private final CodeStyleSettings mySettings;

    public HttpRequestBodyGroupBlock(ASTNode node, CodeStyleSettings mySettings) {
        super(node);
        this.mySettings = mySettings;
    }

    @NotNull
    @Override
    protected Block createBlock(@NotNull ASTNode node) {
        IElementType type = node.getElementType();
        if (type == HttpTypes.MESSAGE_BODY) {
            return new HttpRequestBodyBlock(node, mySettings);
        } else {
            return super.createBlock(node);
        }
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        return null;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
