package org.javamaster.httpclient.impl.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.template.context.BaseTemplateContextType;
import consulo.language.editor.template.context.TemplateActionContext;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.localize.LocalizeValue;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.highlighting.support.HttpSyntaxHighlighter;
import org.javamaster.httpclient.psi.HttpPsiUtils;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class HttpTemplateContextType extends BaseTemplateContextType {

    public HttpTemplateContextType() {
        super("REQUEST", LocalizeValue.localizeTODO("Http request"));
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return inContext(templateActionContext.getFile(), templateActionContext.getStartOffset());
    }

    private boolean inContext(PsiFile file, int offset) {
        if (!PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(HttpLanguage.INSTANCE)) {
            return false;
        }

        PsiElement element = file.findElementAt(offset);
        if (element instanceof PsiWhiteSpace && offset > 0) {
            element = file.findElementAt(offset - 1);
        }

        return element != null && inContext(element);
    }

    private boolean inContext(PsiElement element) {
        return HttpPsiUtils.isOfType(element, HttpTypes.REQUEST_METHOD);
    }

    @Nullable
    @Override
    public SyntaxHighlighter createHighlighter() {
        return new HttpSyntaxHighlighter();
    }
}
