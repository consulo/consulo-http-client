package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SettingsAwareBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpDirectionCommentBlock extends HttpRequestBaseBlock implements SettingsAwareBlock {

    private final CodeStyleSettings mySettings;

    public HttpDirectionCommentBlock(ASTNode fileNode, CodeStyleSettings mySettings) {
        super(fileNode);
        this.mySettings = mySettings;
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
