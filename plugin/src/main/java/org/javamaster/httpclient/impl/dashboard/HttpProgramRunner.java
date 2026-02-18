package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.executor.ExecutorRegistry;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunnerSettings;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.impl.internal.ExecutionManagerImpl;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.GenericProgramRunner;
import consulo.execution.ui.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.codeEditor.EditorGutterComponentEx;
import consulo.project.Project;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.runconfig.HttpRunProfileState;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

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
        Runnable loadingRemover = gutterComponent != null ? gutterComponent.setLoadingIconForCurrentGutterMark() : null;

        Project project = httpMethod.getProject();

        if (httpMethod.getContainingFile().getVirtualFile().getFileSystem() instanceof JarFileSystem) {
            NotifyUtil.notifyWarn(project, NlsBundle.message("template.not.execute"));
            if (loadingRemover != null) {
                loadingRemover.run();
            }
            return;
        }

        String tabName = HttpUtils.getTabName(httpMethod);

        try {
            // tabName会用作文件名,因此需要检测下
            Path.of(tabName);

            if (tabName.contains("/") || tabName.contains("\\")) {
                throw new InvalidPathException(tabName, NlsBundle.message("tab.name.error", "Illegal char: / \\"));
            }
        } catch (InvalidPathException e) {
            NotifyUtil.notifyError(project, NlsBundle.message("tab.name.error", e.getMessage()));
            if (loadingRemover != null) {
                loadingRemover.run();
            }
            return;
        }

        httpMethod.putUserData(HttpUtils.gutterIconLoadingKey, loadingRemover);

        com.intellij.execution.Executor httpExecutor = ExecutorRegistry.getInstance().getExecutorById(HttpExecutor.HTTP_EXECUTOR_ID);

        String selectedEnv = HttpEditorTopForm.getSelectedEnv(httpMethod.getProject());

        com.intellij.execution.RunnerAndConfigurationSettings runnerAndConfigurationSettings =
            HttpUtils.saveConfiguration(tabName, project, selectedEnv, httpMethod);

        ExecutionEnvironment environment = new ExecutionEnvironment(httpExecutor, this, runnerAndConfigurationSettings, project);

        execute(environment);
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) {
        if (!(state instanceof HttpRunProfileState)) {
            return null;
        }

        com.intellij.execution.RunnerAndConfigurationSettings runnerAndConfigurationSettings =
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

        com.intellij.execution.ExecutionResult executionResult;
        try {
            executionResult = state.execute(environment.getExecutor(), this);
        } catch (Exception e) {
            return null;
        }

        if (executionResult == null) {
            return null;
        }

        HttpProcessHandler handler = (HttpProcessHandler) executionResult.getProcessHandler();

        RunContentDescriptor contentToReuse = ExecutionManagerImpl.getAllDescriptors(environment.getProject())
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
