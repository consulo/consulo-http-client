package org.javamaster.httpclient.impl.dashboard;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.EditorGutterComponentEx;
import consulo.execution.ExecutionManager;
import consulo.execution.ExecutionResult;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunnerSettings;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.executor.Executor;
import consulo.execution.executor.ExecutorRegistry;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.GenericProgramRunner;
import consulo.execution.runner.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.runconfig.HttpRunProfileState;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@ExtensionImpl
public class HttpProgramRunner extends GenericProgramRunner<RunnerSettings> {
    public static final String HTTP_RUNNER_ID = "HttpProgramRunner";

    @Override
    public @NotNull String getRunnerId() {
        return HTTP_RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (!(profile instanceof HttpRunConfiguration)) {
            return false;
        }
        return DefaultRunExecutor.EXECUTOR_ID.equals(executorId);
    }

    public void executeFromGutter(HttpMethod httpMethod, EditorGutterComponentEx gutterComponent) {
        Project project = httpMethod.getProject();

        if (httpMethod.getContainingFile().getVirtualFile().getFileSystem() instanceof ArchiveFileSystem) {
            NotifyUtil.notifyWarn(project, HttpClientLocalize.templateNotExecute().get());
            return;
        }

        String tabName = HttpUtilsPart.getTabName(httpMethod);

        try {
            // tabName会用作文件名,因此需要检测下
            Path.of(tabName);

            if (tabName.contains("/") || tabName.contains("\\")) {
                throw new InvalidPathException(tabName, HttpClientLocalize.tabNameError("Illegal char: / \\").get());
            }
        }
        catch (InvalidPathException e) {
            NotifyUtil.notifyError(project, HttpClientLocalize.tabNameError(e.getMessage()).get());
            return;
        }

        Executor httpExecutor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);

        String selectedEnv = HttpEditorTopForm.getSelectedEnv(httpMethod.getProject());

        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
            HttpUtils.saveConfiguration(tabName, project, selectedEnv, httpMethod);

        ExecutionEnvironment environment = new ExecutionEnvironment(httpExecutor, this, runnerAndConfigurationSettings, project);

        try {
            execute(environment);
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) {
        if (!(state instanceof HttpRunProfileState)) {
            return null;
        }

        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
            environment.getRunnerAndConfigurationSettings();
        if (runnerAndConfigurationSettings != null) {
            HttpRunConfiguration httpRunConfiguration = (HttpRunConfiguration) runnerAndConfigurationSettings.getConfiguration();
            HttpEditorTopForm.setCurrentEditorSelectedEnv(
                httpRunConfiguration.getHttpFilePath(),
                environment.getProject(),
                httpRunConfiguration.getEnv()
            );
        }

        environment.setExecutionId(0);

        ExecutionResult executionResult;
        try {
            executionResult = state.execute(environment.getExecutor(), this);
        }
        catch (Exception e) {
            return null;
        }

        if (executionResult == null) {
            return null;
        }

        HttpProcessHandler handler = (HttpProcessHandler) executionResult.getProcessHandler();

        RunContentDescriptor contentToReuse = ExecutionManager.getInstance(environment.getProject()).getDescriptors(r -> true)
            .stream()
            .filter(it ->
                it.getProcessHandler() instanceof HttpProcessHandler &&
                    it.getDisplayName().equals(handler.tabName)
            )
            .findFirst()
            .orElse(null);

        environment.setContentToReuse(contentToReuse);

        return new RunContentBuilder(executionResult, environment).showRunContent(environment.getContentToReuse());
    }
}
