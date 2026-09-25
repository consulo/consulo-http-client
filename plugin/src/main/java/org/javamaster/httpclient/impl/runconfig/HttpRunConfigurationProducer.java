package org.javamaster.httpclient.impl.runconfig;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Configuration of the request at the caret - or of all requests of the file, when the target is the file itself
 * (in project view) or a place out of the requests.
 * <p>
 * The request is bound by the file path and its tab name, the name of the configuration may be made unique.
 *
 * @author VISTALL
 * @since 2026-09-25
 */
@ExtensionImpl
public class HttpRunConfigurationProducer extends RunConfigurationProducer<HttpRunConfiguration> {
    public HttpRunConfigurationProducer() {
        super(ConfigurationTypeUtil.findConfigurationType(HttpConfigurationType.class));
    }

    @Override
    @RequiredReadAction
    protected boolean setupConfigurationFromContext(
        HttpRunConfiguration configuration,
        ConfigurationContext context,
        SimpleReference<PsiElement> sourceElement
    ) {
        PsiElement element = context.getPsiLocation();

        HttpFile httpFile = findRunnableFile(element);
        if (httpFile == null) {
            return false;
        }

        HttpMethod httpMethod = findHttpMethod(element);
        if (httpMethod != null) {
            configuration.setRunAll(false);
            configuration.setRequestName(HttpUtilsPart.getTabName(httpMethod));
            sourceElement.set(httpMethod);
        }
        else {
            if (httpFile.getRequestBlocks().isEmpty()) {
                return false;
            }

            configuration.setRunAll(true);
            configuration.setRequestName("");
            sourceElement.set(httpFile);
        }

        configuration.setHttpFilePath(httpFile.getVirtualFile().getPath());
        configuration.setEnv(StringUtil.notNullize(HttpEditorTopForm.getSelectedEnv(context.getProject())));
        // the name is suggested by the request or by the file - so after them
        configuration.setGeneratedName();
        return true;
    }

    @Override
    @RequiredReadAction
    public boolean isConfigurationFromContext(HttpRunConfiguration configuration, ConfigurationContext context) {
        PsiElement element = context.getPsiLocation();

        HttpFile httpFile = findRunnableFile(element);
        if (httpFile == null || !Objects.equals(configuration.getHttpFilePath(), httpFile.getVirtualFile().getPath())) {
            return false;
        }

        HttpMethod httpMethod = findHttpMethod(element);
        if (httpMethod == null) {
            return configuration.isRunAll();
        }

        return !configuration.isRunAll() && Objects.equals(configuration.getRequestName(), HttpUtilsPart.getTabName(httpMethod));
    }

    @RequiredReadAction
    private static @Nullable HttpFile findRunnableFile(@Nullable PsiElement element) {
        if (element == null || !(element.getContainingFile() instanceof HttpFile httpFile)) {
            return null;
        }

        // requests of the history and of the bundled examples are not runnable - same as from the gutter
        VirtualFile virtualFile = httpFile.getVirtualFile();
        if (virtualFile == null
            || virtualFile.getFileSystem() instanceof ArchiveFileSystem
            || HttpUtilsPart.isFileInIdeaDir(virtualFile)) {
            return null;
        }
        return httpFile;
    }

    /**
     * @return null when the target is out of the requests
     */
    @RequiredReadAction
    private static @Nullable HttpMethod findHttpMethod(PsiElement element) {
        HttpRequestBlock requestBlock = PsiTreeUtil.getParentOfType(element, HttpRequestBlock.class, false);
        HttpRequest request = requestBlock != null ? requestBlock.getRequest() : null;
        return request != null ? request.getMethod() : null;
    }
}
