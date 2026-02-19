package org.javamaster.httpclient.impl.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.context.BaseTemplateContextType;
import consulo.language.editor.template.context.TemplateActionContext;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpJsHandlerTemplateContextType extends BaseTemplateContextType {

    public HttpJsHandlerTemplateContextType() {
        super("REQUEST_RESPONSE_HANDLER_PATH", LocalizeValue.localizeTODO("Http js handler"), HttpTemplateContextType.class);
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
