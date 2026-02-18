package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.psi.HttpDirectionComment;
import org.javamaster.httpclient.psi.HttpDirectionValue;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpDirectionValuePsiReference extends PsiReferenceBase<HttpDirectionValue> {

    public HttpDirectionValuePsiReference(@NotNull HttpDirectionValue directionValue, TextRange textRange) {
        super(directionValue, textRange);
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

        var directionComment = (HttpDirectionComment) getElement().getParent();
        var project = getElement().getProject();

        var path = HttpUtils.getDirectionPath(directionComment, parentPath);
        if (path == null) {
            return null;
        }

        return HttpUtils.resolveFilePath(path, parentPath, project);
    }

}
