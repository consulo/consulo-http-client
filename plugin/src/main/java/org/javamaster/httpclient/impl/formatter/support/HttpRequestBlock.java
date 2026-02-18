package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SettingsAwareBlock;
import consulo.language.ast.IElementType;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpRequestBlock extends HttpRequestBaseBlock implements SettingsAwareBlock {

    private final CodeStyleSettings mySettings;

    public HttpRequestBlock(ASTNode node, CodeStyleSettings mySettings) {
        super(node);
        this.mySettings = mySettings;
    }

    @NotNull
    @Override
    protected Block createBlock(@NotNull ASTNode node) {
        IElementType type = node.getElementType();
        if (type == HttpTypes.REQUEST_MESSAGES_GROUP) {
            return new HttpRequestBodyGroupBlock(node, getSettings());
        } else if (type == HttpTypes.REQUEST_TARGET) {
            return new HttpRequestTargetBlock(node);
        } else if (type == HttpTypes.RESPONSE_HANDLER || type == HttpTypes.PRE_REQUEST_HANDLER) {
            return new HttpHandlerBlock(node);
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

    @NotNull
    @Override
    public CodeStyleSettings getSettings() {
        return mySettings;
    }
}
