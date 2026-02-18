package org.javamaster.httpclient.impl.suppress;

import consulo.language.editor.rawHighlight.HighlightErrorFilter;
import consulo.json.lang.JsonLanguage;
import consulo.language.Language;
import consulo.language.inject.InjectedLanguageManager;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class MyHighlightErrorFilter extends HighlightErrorFilter {
    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element) {
        Language language = element.getLanguage();
        Project project = element.getProject();

        if (language == JsonLanguage.INSTANCE) {
            PsiElement injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element);
            return !(injectionHost instanceof HttpMessageBody);
        }

        return true;
    }
}
