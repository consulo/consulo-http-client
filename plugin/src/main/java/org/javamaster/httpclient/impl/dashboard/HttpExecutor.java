package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.localize.ExecutionLocalize;
import consulo.execution.executor.Executor;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.project.ui.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HttpExecutor extends Executor {
    public static final String HTTP_EXECUTOR_ID = "httpExecutor";

    @Override
    public @NotNull String getToolWindowId() {
        return ToolWindowId.SERVICES;
    }

    @Override
    public @NotNull Icon getToolWindowIcon() {
        return PlatformIconGroup.toolwindowsToolWindowRun;
    }

    @Override
    public @NotNull Icon getIcon() {
        return PlatformIconGroup.actionsExecute;
    }

    @Override
    public Icon getRerunIcon() {
        return PlatformIconGroup.actionsRerun;
    }

    @Override
    public Icon getDisabledIcon() {
        return IconLoader.getDisabledIcon(getIcon());
    }

    @Override
    public @NotNull String getDescription() {
        return "Run selected configuration";
    }

    @Override
    public @NotNull String getActionName() {
        return "Run";
    }

    @Override
    public @NotNull String getId() {
        return HTTP_EXECUTOR_ID;
    }

    @Override
    public @NotNull String getStartActionText() {
        return ExecutionBundle.message("default.runner.start.action.text");
    }

    @Override
    public String getContextActionId() {
        return "RunRequest";
    }

    @Override
    public String getHelpId() {
        return "ideaInterface.run";
    }
}
