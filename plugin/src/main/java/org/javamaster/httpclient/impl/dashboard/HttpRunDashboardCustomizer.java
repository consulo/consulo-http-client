package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.dashboard.RunDashboardCustomizer;
import consulo.execution.dashboard.RunDashboardRunConfigurationNode;
import consulo.execution.ui.RunContentDescriptor;
import consulo.ui.ex.tree.PresentationData;
import consulo.language.psi.PsiElement;
import consulo.ui.ex.SimpleTextAttributes;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Support Service tool window double click action, jump to request of the corresponding file.
 *
 * @author yudong
 */
public class HttpRunDashboardCustomizer extends RunDashboardCustomizer {

    @Override
    public boolean isApplicable(RunnerAndConfigurationSettings settings, @Nullable RunContentDescriptor descriptor) {
        return settings.getConfiguration() instanceof HttpRunConfiguration;
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(RunDashboardRunConfigurationNode node) {
        HttpRunConfiguration configuration = (HttpRunConfiguration) node.getConfigurationSettings().getConfiguration();
        return HttpUtils.getTargetHttpMethod(configuration.getHttpFilePath(), configuration.getName(), configuration.getProject());
    }

    @Override
    public boolean updatePresentation(PresentationData presentation, RunDashboardRunConfigurationNode node) {
        RunContentDescriptor descriptor = node.getDescriptor();
        if (descriptor == null) {
            return false;
        }

        HttpProcessHandler processHandler = (HttpProcessHandler) descriptor.getProcessHandler();
        if (processHandler == null) {
            return false;
        }

        if (processHandler.hasError) {
            presentation.addText(" Status: Error in the request", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            return true;
        }

        Integer status = processHandler.httpStatus;
        if (status == null) {
            return false;
        }

        Icon icon = HttpUtils.pickMethodIcon(processHandler.httpMethod.getText());

        presentation.setIcon(icon);

        Long costTimes = processHandler.costTimes;
        presentation.addText(" Status: " + status + "(" + costTimes + " ms)", SimpleTextAttributes.GRAYED_ATTRIBUTES);

        return true;
    }
}
