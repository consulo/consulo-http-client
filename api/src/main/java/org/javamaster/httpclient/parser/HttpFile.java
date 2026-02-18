package org.javamaster.httpclient.parser;

import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.virtualFileSystem.fileType.FileType;
import org.javamaster.httpclient.HttpFileType;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.psi.HttpDirectionComment;
import org.javamaster.httpclient.psi.HttpGlobalHandler;
import org.javamaster.httpclient.psi.HttpGlobalVariable;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author yudong
 */
public class HttpFile extends PsiFileBase {

    public HttpFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, HttpLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return HttpFileType.INSTANCE;
    }

    @Nullable
    public HttpGlobalHandler getGlobalHandler() {
        return PsiTreeUtil.getChildOfType(this, HttpGlobalHandler.class);
    }

    @NotNull
    public List<HttpGlobalVariable> getGlobalVariables() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpGlobalVariable.class);
    }

    @NotNull
    public List<HttpRequestBlock> getRequestBlocks() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpRequestBlock.class);
    }

    @NotNull
    public List<HttpDirectionComment> getDirectionComments() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpDirectionComment.class);
    }

    @Override
    public String toString() {
        return "HTTP File";
    }
}
