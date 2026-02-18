package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.*;
import consulo.language.ast.ASTNode;
import consulo.document.util.TextRange;
import consulo.language.ast.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public abstract class HttpRequestBaseBlock implements ASTBlock {
    private static final ChildAttributes NONE = new ChildAttributes(Indent.getAbsoluteNoneIndent(), null);

    private final ASTNode myNode;
    private final Indent myIndent = Indent.getAbsoluteNoneIndent();
    private List<Block> myChildren;

    public HttpRequestBaseBlock(ASTNode myNode) {
        this.myNode = myNode;
    }

    @NotNull
    protected List<Block> getSubBlocksInternal() {
        ASTNode[] nodes = getNode().getChildren(null);
        List<Block> blocks = new ArrayList<>();

        for (ASTNode node : nodes) {
            if (node.getElementType() != TokenType.WHITE_SPACE) {
                blocks.add(createBlock(node));
            }
        }

        return blocks;
    }

    @NotNull
    @Override
    public List<Block> getSubBlocks() {
        if (myChildren == null) {
            myChildren = getSubBlocksInternal();
        }

        return myChildren;
    }

    @NotNull
    protected Block createBlock(@NotNull ASTNode node) {
        return new HttpRequestLeafBlock(node);
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return myNode.getTextRange();
    }

    @Nullable
    @Override
    public Indent getIndent() {
        return myIndent;
    }

    @Nullable
    @Override
    public Alignment getAlignment() {
        return null;
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @NotNull
    @Override
    public ASTNode getNode() {
        return myNode;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return NONE;
    }
}
