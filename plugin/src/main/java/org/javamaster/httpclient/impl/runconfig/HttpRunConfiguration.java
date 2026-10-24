package org.javamaster.httpclient.impl.runconfig;

import consulo.application.ReadAction;
import consulo.execution.configuration.*;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.run.HttpRunConfigurationApi;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author yudong
 */
public class HttpRunConfiguration extends LocatableConfigurationBase implements HttpRunConfigurationApi {
    /**
     * Environment of the run instead of the one of the configuration - the requests run by the run of all requests of the file
     * are with the environment of that run
     */
    public static final Key<String> RUN_ENV_KEY = Key.create("HttpRunConfiguration.runEnv");

    private static final String ENV_KEY = "env";
    private static final String PATH_KEY = "httpFilePath";
    private static final String REQUEST_NAME_KEY = "requestName";
    private static final String RUN_ALL_KEY = "runAll";

    public HttpRunConfiguration(Project project, ConfigurationFactory httpConfigurationFactory, String name) {
        super(project, httpConfigurationFactory, name);
    }

    @Override
    protected Class<HttpRunConfigurationOptions> getOptionsClass() {
        return HttpRunConfigurationOptions.class;
    }

    @Override
    public HttpRunConfigurationOptions getOptions() {
        return (HttpRunConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        Element element = converter.getElement();
        HttpRunConfigurationOptions options = new HttpRunConfigurationOptions();
        options.setEnv(element.getChildText(ENV_KEY));
        options.setHttpFilePath(element.getChildText(PATH_KEY));
        options.setRequestName(element.getChildText(REQUEST_NAME_KEY));
        options.setRunAll(Boolean.parseBoolean(element.getChildText(RUN_ALL_KEY)));
        converter.setOptions(options);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(new HttpRequestFragment());
    }

    @Override
    public String getHttpFilePath() {
        return StringUtil.notNullize(getOptions().getHttpFilePath());
    }

    public void setHttpFilePath(String httpFilePath) {
        getOptions().setHttpFilePath(httpFilePath);
    }

    public String getEnv() {
        return StringUtil.notNullize(getOptions().getEnv());
    }

    public void setEnv(String env) {
        getOptions().setEnv(env);
    }

    public String getEnv(ExecutionEnvironment environment) {
        String runEnv = environment.getUserData(RUN_ENV_KEY);
        return runEnv != null ? runEnv : getEnv();
    }

    @Override
    public LocalizeValue getRequestName() {
        // configurations saved before the request name was stored are bound by their own name
        String requestName = getOptions().getRequestName();
        return requestName == null ? LocalizeValue.of(getName()) : LocalizeValue.of(requestName);
    }

    public void setRequestName(LocalizeValue requestName) {
        getOptions().setRequestName(requestName.get());
    }

    /**
     * All requests of the file are run one by one, instead of the one of {@link #getRequestName()}
     */
    public boolean isRunAll() {
        return getOptions().isRunAll();
    }

    public void setRunAll(boolean runAll) {
        getOptions().setRunAll(runAll);
    }

    @Override
    public @Nullable String suggestedName() {
        if (isRunAll()) {
            String httpFilePath = getHttpFilePath();
            return httpFilePath.isEmpty() ? null : new File(httpFilePath).getName();
        }
        return getOptions().getRequestName();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationError {
        String httpFilePath = getHttpFilePath();
        if (isRunAll()) {
            boolean hasRequests = ReadAction.compute(() -> {
                HttpFile httpFile = HttpUtilsPart.findHttpFile(httpFilePath, getProject());
                return httpFile != null && !httpFile.getRequestBlocks().isEmpty();
            });
            if (!hasRequests) {
                throw new RuntimeConfigurationError(HttpClientLocalize.noRequest());
            }
            return;
        }

        String tabNameError = HttpUtilsPart.getTabNameError(getRequestName());
        if (tabNameError != null) {
            throw new RuntimeConfigurationError(HttpClientLocalize.tabNameError(tabNameError));
        }

        if (ReadAction.compute(() -> HttpUtilsPart.getTargetHttpMethod(httpFilePath, getRequestName(), getProject())) == null) {
            throw new RuntimeConfigurationError(HttpClientLocalize.noRequest());
        }
    }

    @Override
    public RunProfileState getState(Executor executor, ExecutionEnvironment environment) {
        String runEnv = getEnv(environment);
        if (isRunAll()) {
            return new HttpRunAllProfileState(getProject(), getHttpFilePath(), runEnv);
        }
        return new HttpRunProfileState(getProject(), getHttpFilePath(), getRequestName(), runEnv);
    }
}
