package org.javamaster.httpclient.impl.runconfig;

import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ProgramRunner;
import consulo.project.Project;
import org.javamaster.httpclient.impl.dashboard.HttpExecutionResult;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;

/**
 * @author yudong
 */
public class HttpRunProfileState implements RunProfileState {
    private final Project project;
    private final String httpFilePath;
    private final String requestName;
    private final String selectedEnv;

    public HttpRunProfileState(
        Project project,
        String httpFilePath,
        String requestName,
        String selectedEnv
    ) {
        this.project = project;
        this.httpFilePath = httpFilePath;
        this.requestName = requestName;
        this.selectedEnv = selectedEnv;
    }

    @Override
    public ExecutionResult execute(Executor executor, ProgramRunner runner) {
        HttpMethod httpMethod = HttpUtilsPart.getTargetHttpMethod(
            httpFilePath,
            requestName,
            project
        );

        if (httpMethod == null) {
            return null;
        }

        return new HttpExecutionResult(httpMethod, selectedEnv);
    }
}
