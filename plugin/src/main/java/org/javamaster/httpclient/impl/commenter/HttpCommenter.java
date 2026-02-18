package org.javamaster.httpclient.impl.commenter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.CodeDocumentationAwareCommenterEx;
import consulo.language.Language;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpCommenter implements CodeDocumentationAwareCommenterEx {

    private static final String SLASH_COMMENT_PREFIX = "//";
    private static final String BLOCK_COMMENT_START = "/*";
    private static final String BLOCK_COMMENT_END = "*/";

    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return SLASH_COMMENT_PREFIX;
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return BLOCK_COMMENT_START;
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return BLOCK_COMMENT_END;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Nullable
    @Override
    public IElementType getLineCommentTokenType() {
        return HttpTypes.LINE_COMMENT;
    }

    @Nullable
    @Override
    public IElementType getBlockCommentTokenType() {
        return HttpTypes.BLOCK_COMMENT;
    }

    @Nullable
    @Override
    public IElementType getDocumentationCommentTokenType() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentLinePrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationCommentSuffix() {
        return null;
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        return false;
    }

    @Override
    public boolean isDocumentationCommentText(PsiElement element) {
        return false;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
