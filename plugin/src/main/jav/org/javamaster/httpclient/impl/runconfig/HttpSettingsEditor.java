package org.javamaster.httpclient.impl.runconfig;

import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.util.lang.Pair;
import org.javamaster.httpclient.impl.ui.ConfigSettingsForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

/**
 * @author yudong
 */
public class HttpSettingsEditor extends SettingsEditor<HttpRunConfiguration> {
    private final ConfigSettingsForm configSettingsForm = new ConfigSettingsForm();

    public HttpSettingsEditor(String env, String httpFilePath, Project project) {
        configSettingsForm.initForm(env, httpFilePath, project);
    }

    @Override
    protected void resetEditorFrom(@NotNull HttpRunConfiguration runConfiguration) {
        // No implementation needed
    }

    @Override
    protected void applyEditorTo(@NotNull HttpRunConfiguration runConfiguration) {
        Pair<String, String> pair = configSettingsForm.getPair();

        runConfiguration.setEnv(pair.getFirst());
        runConfiguration.setHttpFilePath(pair.getSecond());
    }

    @Override
    public boolean isReadyForApply() {
        return true;
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return configSettingsForm.getMainPanel();
    }
}
