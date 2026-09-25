package org.javamaster.httpclient.impl.runconfig;

import consulo.application.ReadAction;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.run.HttpRunConfigurationApi;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.io.File;

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

    private String httpFilePath = "";
    private String env = "";
    private String requestName = "";
    private boolean runAll;

    public HttpRunConfiguration(Project project, ConfigurationFactory httpConfigurationFactory, String name) {
        super(project, httpConfigurationFactory, name);
    }

    @Override
    public String getHttpFilePath() {
        return httpFilePath;
    }

    public void setHttpFilePath(String httpFilePath) {
        this.httpFilePath = httpFilePath;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getEnv(ExecutionEnvironment environment) {
        String runEnv = environment.getUserData(RUN_ENV_KEY);
        return runEnv != null ? runEnv : env;
    }

    @Override
    public String getRequestName() {
        // configurations saved before the request name was stored are bound by their own name
        return requestName.isEmpty() ? getName() : requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * All requests of the file are run one by one, instead of the one of {@link #getRequestName()}
     */
    public boolean isRunAll() {
        return runAll;
    }

    public void setRunAll(boolean runAll) {
        this.runAll = runAll;
    }

    @Override
    public @Nullable String suggestedName() {
        if (runAll) {
            return httpFilePath.isEmpty() ? null : new File(httpFilePath).getName();
        }
        return requestName.isEmpty() ? null : requestName;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationError {
        if (runAll) {
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
        if (runAll) {
            return new HttpRunAllProfileState(getProject(), httpFilePath, runEnv);
        }
        return new HttpRunProfileState(getProject(), httpFilePath, getRequestName(), runEnv);
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new HttpSettingsEditor(env, httpFilePath, getProject());
    }

    @Override
    public void writeExternal(Element element) {
        Element envEle = new Element(ENV_KEY);
        envEle.setText(env);
        element.addContent(envEle);

        Element pathEle = new Element(PATH_KEY);
        pathEle.setText(httpFilePath);
        element.addContent(pathEle);

        Element requestNameEle = new Element(REQUEST_NAME_KEY);
        requestNameEle.setText(requestName);
        element.addContent(requestNameEle);

        Element runAllEle = new Element(RUN_ALL_KEY);
        runAllEle.setText(String.valueOf(runAll));
        element.addContent(runAllEle);

        super.writeExternal(element);
    }

    @Override
    public void readExternal(Element element) {
        super.readExternal(element);

        Element envChild = element.getChild(ENV_KEY);
        env = envChild != null ? envChild.getText() : "";

        Element pathChild = element.getChild(PATH_KEY);
        httpFilePath = pathChild != null ? pathChild.getText() : "";

        Element requestNameChild = element.getChild(REQUEST_NAME_KEY);
        requestName = requestNameChild != null ? requestNameChild.getText() : "";

        Element runAllChild = element.getChild(RUN_ALL_KEY);
        runAll = runAllChild != null && Boolean.parseBoolean(runAllChild.getText());
    }
}
