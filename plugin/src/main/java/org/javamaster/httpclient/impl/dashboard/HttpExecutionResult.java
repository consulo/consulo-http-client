package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.ExecutionResult;
import consulo.process.ProcessHandler;
import consulo.execution.ui.ExecutionConsole;
import consulo.ui.ex.action.AnAction;
import org.javamaster.httpclient.psi.HttpMethod;
import org.jetbrains.annotations.NotNull;

public class HttpExecutionResult implements ExecutionResult {
    private final HttpProcessHandler httpProcessHandler;

    public HttpExecutionResult(HttpMethod httpMethod, String selectedEnv) {
        this.httpProcessHandler = new HttpProcessHandler(httpMethod, selectedEnv);
    }

    @Override
    public ExecutionConsole getExecutionConsole() {
        return new HttpExecutionConsole(httpProcessHandler.getComponent());
    }

    @Override
    public AnAction @NotNull [] getActions() {
        return new AnAction[0];
    }

    @Override
    public ProcessHandler getProcessHandler() {
        return httpProcessHandler;
    }
}
