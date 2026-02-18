package org.javamaster.httpclient.impl.formatter.support;

import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Indent;
import consulo.language.codeStyle.Spacing;
import consulo.language.codeStyle.Wrap;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.DefaultInjectedLanguageBlockBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
public class HttpRequestBodyBlock extends HttpRequestBaseBlock {
    private final DefaultInjectedLanguageBlockBuilder myInjectedBlockBuilder;

    public HttpRequestBodyBlock(ASTNode node, CodeStyleSettings settings) {
        super(node);
        this.myInjectedBlockBuilder = new DefaultInjectedLanguageBlockBuilder(settings);
    }

    @NotNull
    @Override
    protected List<Block> getSubBlocksInternal() {
        List<Block> result = new ArrayList<>();

        myInjectedBlockBuilder.addInjectedBlocks(
                result,
                getNode(),
                getWrap(),
                getAlignment(),
                Indent.getAbsoluteNoneIndent()
        );
        return result;
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
