package org.javamaster.httpclient.impl.runconfig;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ProgramRunner;
import consulo.execution.ui.console.ConsoleView;
import consulo.execution.ui.console.TextConsoleBuilderFactory;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.project.Project;
import org.javamaster.httpclient.impl.dashboard.HttpProgramRunner;
import org.javamaster.httpclient.impl.dashboard.HttpRunAllProcessHandler;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs all requests of the file one by one - the progress is in the console, each request is the run of its own configuration.
 *
 * @author VISTALL
 * @since 2026-09-25
 */
public class HttpRunAllProfileState implements RunProfileState {
    private final Project myProject;
    private final String myHttpFilePath;
    private final String myEnv;

    public HttpRunAllProfileState(Project project, String httpFilePath, String env) {
        myProject = project;
        myHttpFilePath = httpFilePath;
        myEnv = env;
    }

    @Override
    public @Nullable ExecutionResult execute(Executor executor, ProgramRunner runner) {
        if (!(runner instanceof HttpProgramRunner httpProgramRunner)) {
            return null;
        }

        List<SmartPsiElementPointer<HttpMethod>> httpMethods = ReadAction.compute(this::findHttpMethods);
        if (httpMethods.isEmpty()) {
            return null;
        }

        HttpRunAllProcessHandler processHandler = new HttpRunAllProcessHandler(myProject, httpProgramRunner, httpMethods, myEnv);

        ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(myProject).getConsole();
        console.attachToProcess(processHandler);

        return new DefaultExecutionResult(console, processHandler);
    }

    @RequiredReadAction
    private List<SmartPsiElementPointer<HttpMethod>> findHttpMethods() {
        HttpFile httpFile = HttpUtilsPart.findHttpFile(myHttpFilePath, myProject);
        if (httpFile == null) {
            return List.of();
        }

        SmartPointerManager pointerManager = SmartPointerManager.getInstance(myProject);

        List<SmartPsiElementPointer<HttpMethod>> httpMethods = new ArrayList<>();
        for (HttpRequestBlock requestBlock : httpFile.getRequestBlocks()) {
            HttpRequest request = requestBlock.getRequest();
            HttpMethod httpMethod = request != null ? request.getMethod() : null;
            if (httpMethod != null) {
                httpMethods.add(pointerManager.createSmartPsiElementPointer(httpMethod));
            }
        }
        return httpMethods;
    }
}
