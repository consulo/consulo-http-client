package org.javamaster.httpclient.impl.runconfig;

import consulo.application.ReadAction;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.project.Project;
import org.javamaster.httpclient.run.HttpRunConfigurationApi;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jdom.Element;

/**
 * @author yudong
 */
public class HttpRunConfiguration extends RunConfigurationBase implements HttpRunConfigurationApi {
    private static final String ENV_KEY = "env";
    private static final String PATH_KEY = "httpFilePath";

    private String httpFilePath = "";
    private String env = "";

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

    @Override
    public void checkConfiguration() throws RuntimeConfigurationError {
        if (ReadAction.compute(() -> HttpUtilsPart.getTargetHttpMethod(httpFilePath, getName(), getProject())) == null) {
            throw new RuntimeConfigurationError(HttpClientLocalize.noRequest());
        }
    }

    @Override
    public RunProfileState getState(Executor executor, ExecutionEnvironment environment) {
        return new HttpRunProfileState(getProject(), environment, httpFilePath, env);
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

        super.writeExternal(element);
    }

    @Override
    public void readExternal(Element element) {
        super.readExternal(element);

        Element envChild = element.getChild(ENV_KEY);
        env = envChild != null ? envChild.getText() : "";

        Element pathChild = element.getChild(PATH_KEY);
        httpFilePath = pathChild != null ? pathChild.getText() : "";
    }
}
