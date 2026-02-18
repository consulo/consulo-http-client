package org.javamaster.httpclient.impl.liveTemplates;

import consulo.language.editor.template.TemplateActionContext;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpEnvFileTemplateContextType extends TemplateContextType {

    protected HttpEnvFileTemplateContextType() {
        super("Http env file");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return inContext(templateActionContext.getFile());
    }

    private boolean inContext(PsiFile file) {
        return EnvFileService.ENV_FILE_NAMES.contains(file.getName());
    }
}
