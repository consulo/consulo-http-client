package org.javamaster.httpclient.impl.dashboard;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
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
import consulo.execution.runner.ExecutionEnvironmentBuilder;
import consulo.execution.runner.GenericProgramRunner;
import consulo.execution.runner.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import org.javamaster.httpclient.impl.runconfig.HttpRunAllProfileState;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.runconfig.HttpRunProfileState;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public void executeFromGutter(HttpMethod httpMethod) {
        if (!canRun(httpMethod)) {
            return;
        }

        Project project = httpMethod.getProject();

        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
            HttpUtils.saveConfiguration(project, HttpEditorTopForm.getSelectedEnv(project), httpMethod);
        if (runnerAndConfigurationSettings == null) {
            return;
        }

        try {
            execute(createEnvironment(project, runnerAndConfigurationSettings));
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Environment of the run of the request as a part of another run - the configuration of the request is not stored
     *
     * @return null when the request can't be run - the reason is notified
     */
    public @Nullable ExecutionEnvironment createEnvironment(HttpMethod httpMethod, String env) {
        if (!canRun(httpMethod)) {
            return null;
        }

        RunnerAndConfigurationSettings runnerAndConfigurationSettings = HttpUtils.findOrCreateConfiguration(httpMethod);
        if (runnerAndConfigurationSettings == null) {
            return null;
        }

        ExecutionEnvironment environment = createEnvironment(httpMethod.getProject(), runnerAndConfigurationSettings);
        // a stored configuration is not changed - the environment is of this run only
        environment.putUserData(HttpRunConfiguration.RUN_ENV_KEY, env);
        return environment;
    }

    private ExecutionEnvironment createEnvironment(Project project, RunnerAndConfigurationSettings runnerAndConfigurationSettings) {
        Executor httpExecutor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);

        // settings must be passed - without them the run dashboard can't bind the run content to the configuration node
        return new ExecutionEnvironmentBuilder(project, httpExecutor)
            .runnerAndSettings(this, runnerAndConfigurationSettings)
            .build();
    }

    /**
     * @return false when the request can't be run - the reason is notified
     */
    private static boolean canRun(HttpMethod httpMethod) {
        Project project = httpMethod.getProject();

        if (ReadAction.compute(() -> httpMethod.getContainingFile().getVirtualFile().getFileSystem()) instanceof ArchiveFileSystem) {
            NotifyUtil.notifyWarn(project, HttpClientLocalize.templateNotExecute().get());
            return false;
        }

        // tab name is used as a file name, so it must be checked
        String tabNameError = HttpUtilsPart.getTabNameError(ReadAction.compute(() -> HttpUtilsPart.getTabName(httpMethod)));
        if (tabNameError != null) {
            NotifyUtil.notifyError(project, HttpClientLocalize.tabNameError(tabNameError).get());
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) {
        if (!(state instanceof HttpRunProfileState) && !(state instanceof HttpRunAllProfileState)) {
            return null;
        }

        RunnerAndConfigurationSettings runnerAndConfigurationSettings =
            environment.getRunnerAndConfigurationSettings();
        if (runnerAndConfigurationSettings != null) {
            HttpRunConfiguration httpRunConfiguration = (HttpRunConfiguration) runnerAndConfigurationSettings.getConfiguration();
            HttpEditorTopForm.setCurrentEditorSelectedEnv(
                httpRunConfiguration.getHttpFilePath(),
                environment.getProject(),
                httpRunConfiguration.getEnv(environment)
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

        // the content is named by the configuration - unlike the tab name of the request, it is unique
        String displayName = environment.getRunProfile().getName();

        RunContentDescriptor contentToReuse = ExecutionManager.getInstance(environment.getProject()).getDescriptors(r -> true)
            .stream()
            .filter(it ->
                (it.getProcessHandler() instanceof HttpProcessHandler || it.getProcessHandler() instanceof HttpRunAllProcessHandler) &&
                    it.getDisplayName().equals(displayName)
            )
            .findFirst()
            .orElse(null);

        environment.setContentToReuse(contentToReuse);

        return new RunContentBuilder(executionResult, environment).showRunContent(environment.getContentToReuse());
    }
}
