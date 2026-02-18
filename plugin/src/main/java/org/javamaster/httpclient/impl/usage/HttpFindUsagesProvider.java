package org.javamaster.httpclient.impl.usage;

import consulo.language.findUsage.FindUsagesProvider;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpGlobalVariableName;
import org.javamaster.httpclient.psi.HttpVariableName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpFindUsagesProvider implements FindUsagesProvider {

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof HttpVariableName) {
            HttpVariableName variableName = (HttpVariableName) element;
            if (variableName.isBuiltin()) {
                return NlsBundle.message("builtin.variable");
            } else {
                return NlsBundle.message("variable");
            }
        } else if (element instanceof HttpGlobalVariableName) {
            return NlsBundle.message("global.variable");
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof HttpVariableName) {
            return StringUtil.notNullize(element.getText());
        } else if (element instanceof HttpGlobalVariableName) {
            return StringUtil.notNullize(element.getText());
        } else {
            return "";
        }
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof HttpVariableName) {
            return StringUtil.notNullize(element.getText());
        } else if (element instanceof HttpGlobalVariableName) {
            HttpGlobalVariableName globalVariableName = (HttpGlobalVariableName) element;
            return globalVariableName.getName();
        } else {
            return "";
        }
    }
}
