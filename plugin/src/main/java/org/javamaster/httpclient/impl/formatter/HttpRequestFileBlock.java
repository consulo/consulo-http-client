package org.javamaster.httpclient.impl.formatter;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SettingsAwareBlock;
import org.javamaster.httpclient.impl.formatter.support.HttpDirectionCommentBlock;
import org.javamaster.httpclient.impl.formatter.support.HttpRequestBaseBlock;
import org.javamaster.httpclient.impl.formatter.support.HttpRequestGroupBlock;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author yudong
 */
public class HttpRequestFileBlock extends HttpRequestBaseBlock implements SettingsAwareBlock {

    private final CodeStyleSettings mySettings;

    public HttpRequestFileBlock(ASTNode fileNode, CodeStyleSettings mySettings) {
        super(fileNode);
        this.mySettings = mySettings;
    }

    @NotNull
    @Override
    protected Block createBlock(@NotNull ASTNode node) {
        if (node.getElementType() == HttpTypes.REQUEST_BLOCK) {
            return new HttpRequestGroupBlock(node, getSettings());
        } else if (node.getElementType() == HttpTypes.DIRECTION_COMMENT) {
            return new HttpDirectionCommentBlock(node, getSettings());
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
        if (child1 instanceof HttpRequestGroupBlock && child2 instanceof HttpRequestGroupBlock) {
            return Spacing.createSpacing(0, 0, 2, true, 100);
        } else if (child1 instanceof HttpDirectionCommentBlock && child2 instanceof HttpRequestGroupBlock) {
            return Spacing.createSpacing(0, 0, 2, true, 100);
        } else {
            return Spacing.getReadOnlySpacing();
        }
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
