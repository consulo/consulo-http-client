package org.javamaster.httpclient.impl.reference.support;

import consulo.project.Project;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.psi.HttpVariableArg;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpVariableArgPsiReference extends PsiReferenceBase<HttpVariableArg> {

    private final TextRange textRange;

    public HttpVariableArgPsiReference(@NotNull HttpVariableArg variableArg, TextRange textRange) {
        super(variableArg, textRange);
        this.textRange = textRange;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var guessPath = getElement().getValue().toString();

        var httpFile = getElement().getContainingFile();
        var project = httpFile.getProject();

        var virtualFile = httpFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        var parent = virtualFile.getParent();
        if (parent == null) {
            return null;
        }

        var httpFileParentPath = parent.getPath();

        return tryResolvePath(guessPath, httpFileParentPath, project);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Nullable
    public static PsiElement tryResolvePath(String guessPath, String httpFileParentPath, Project project) {
        try {
            return HttpUtils.resolveFilePath(guessPath, httpFileParentPath, project);
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

}
