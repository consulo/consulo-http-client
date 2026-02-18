package org.javamaster.httpclient.impl.jsPlugin;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import org.javamaster.httpclient.impl.jsPlugin.support.JavaScript;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public class JsFacade {
    public static final Set<String> INTERESTED_EXPRESSIONS = new HashSet<>();

    static {
        INTERESTED_EXPRESSIONS.add("request.variables.set");
        INTERESTED_EXPRESSIONS.add("client.global.set");
    }

    private JsFacade() {
    }

    @Nullable
    public static PsiElement resolveJsVariable(
        String variableName,
        Project project,
        List<HttpScriptBody> scriptBodyList
    ) {
        PsiElement jsVariable = JavaScript.resolveJsVariable(variableName, project, scriptBodyList);
        if (jsVariable != null) {
            return jsVariable;
        }
        return null;
    }

    @Nullable
    public static PsiElement createJsVariable(Project project, PsiFile injectedPsiFile, String variableName) {
        PsiElement jsVariable = JavaScript.createJsVariable(project, injectedPsiFile, variableName);
        if (jsVariable != null) {
            return jsVariable;
        }

        return null;
    }

    @Nullable
    public static PsiElement resolveJsVariable(String variableName, List<PreJsFile> preJsFiles) {
        if (preJsFiles.isEmpty()) {
            return null;
        }

        for (PreJsFile file : preJsFiles) {
            PsiReference[] psiReferences = file.getDirectionComment().getDirectionValue() != null ?
                file.getDirectionComment().getDirectionValue().getReferences() : null;

            if (psiReferences == null || psiReferences.length == 0) {
                continue;
            }

            PsiElement resolved = psiReferences[0].resolve();
            if (!(resolved instanceof PsiFile)) {
                continue;
            }

            PsiFile jsFile = (PsiFile) resolved;

            PsiElement jsVariable = JavaScript.resolveJsVariable(variableName, jsFile);
            if (jsVariable != null) {
                return jsVariable;
            }
        }

        return null;
    }

    public static boolean isAvailable() {
        return JavaScript.isAvailable();
    }
}
