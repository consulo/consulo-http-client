package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class HttpRequestLeafBlock extends HttpRequestBaseBlock {
    private final boolean myWithIndent;

    public HttpRequestLeafBlock(ASTNode node) {
        this(node, false);
    }

    public HttpRequestLeafBlock(ASTNode node, boolean myWithIndent) {
        super(node);
        this.myWithIndent = myWithIndent;
    }

    @NotNull
    @Override
    protected List<Block> getSubBlocksInternal() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        return null;
    }

    @Nullable
    @Override
    public Indent getIndent() {
        return this.myWithIndent ? Indent.getNoneIndent() : super.getIndent();
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return null;
    }
}
