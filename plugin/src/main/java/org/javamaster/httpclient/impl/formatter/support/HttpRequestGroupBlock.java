package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SettingsAwareBlock;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpRequestGroupBlock extends HttpRequestBaseBlock implements SettingsAwareBlock {

    private final CodeStyleSettings mySettings;

    public HttpRequestGroupBlock(ASTNode fileNode, CodeStyleSettings mySettings) {
        super(fileNode);
        this.mySettings = mySettings;
    }

    @NotNull
    @Override
    protected Block createBlock(@NotNull ASTNode node) {
        if (node.getElementType() == HttpTypes.REQUEST) {
            return new HttpRequestBlock(node, getSettings());
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
        return Spacing.getReadOnlySpacing();
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
