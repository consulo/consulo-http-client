package org.javamaster.httpclient.impl.suppress;

import consulo.language.editor.inspection.InspectionSuppressor;
import consulo.language.editor.inspection.SuppressQuickFix;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.spellchecker.inspection.SpellCheckingInspection;
import org.javamaster.httpclient.impl.inspection.MyJsonInspection;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 */
public class JsonInspectionSuppressor implements InspectionSuppressor {
    private final Set<String> notSuppressSet;

    public JsonInspectionSuppressor() {
        notSuppressSet = new HashSet<>();
        notSuppressSet.add(SpellCheckingInspection.class.getSimpleName());
        notSuppressSet.add(MyJsonInspection.class.getSimpleName());
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (notSuppressSet.contains(toolId)) {
            return false;
        }

        PsiElement injectionHost = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
        return injectionHost instanceof HttpMessageBody;
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}
