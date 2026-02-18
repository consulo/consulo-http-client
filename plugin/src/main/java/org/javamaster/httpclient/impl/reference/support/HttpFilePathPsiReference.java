package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.psi.HttpFilePath;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpFilePathPsiReference extends PsiReferenceBase<HttpFilePath> {

    public HttpFilePathPsiReference(@NotNull HttpFilePath httpFilePath, TextRange textRange) {
        super(httpFilePath, textRange);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var virtualFile = PsiUtil.getVirtualFile(getElement());
        if (virtualFile == null) {
            return null;
        }

        var parent = virtualFile.getParent();
        if (parent == null) {
            return null;
        }

        var parentPath = parent.getPath();

        var path = "";
        var resolvedPath = HttpUtils.resolvePathOfVariable(getElement().getVariable());
        if (resolvedPath != null) {
            path += resolvedPath;
        }

        var filePathContent = getElement().getFilePathContent();
        if (filePathContent != null) {
            path += filePathContent.getText();
        }

        return HttpUtils.resolveFilePath(path, parentPath, getElement().getProject());
    }

}
