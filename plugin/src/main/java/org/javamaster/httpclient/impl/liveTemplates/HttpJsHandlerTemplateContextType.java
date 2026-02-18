package org.javamaster.httpclient.impl.liveTemplates;

import consulo.language.editor.template.context.TemplateActionContext;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpJsHandlerTemplateContextType extends TemplateContextType {

    protected HttpJsHandlerTemplateContextType() {
        super("Http js handler");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return inContext(templateActionContext.getFile(), templateActionContext.getStartOffset());
    }

    private boolean inContext(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return false;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(file.getProject());

        PsiElement injectionHost = injectedLanguageManager.getInjectionHost(element);

        return injectionHost instanceof HttpScriptBody;
    }
}
