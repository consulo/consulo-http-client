package org.javamaster.httpclient.impl.runconfig;

import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.Pair;
import org.javamaster.httpclient.impl.ui.ConfigSettingsForm;
import org.jspecify.annotations.Nullable;

public class HttpSettingsEditor extends SettingsEditor<HttpRunConfiguration> {
    private final String myEnv;
    private final String myHttpFilePath;
    private final Project myProject;

    private @Nullable ConfigSettingsForm myConfigSettingsForm;

    public HttpSettingsEditor(String env, String httpFilePath, Project project) {
        myEnv = env;
        myHttpFilePath = httpFilePath;
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(HttpRunConfiguration runConfiguration) {
    }

    @Override
    protected void applyEditorTo(HttpRunConfiguration runConfiguration) {
        ConfigSettingsForm configSettingsForm = myConfigSettingsForm;
        if (configSettingsForm == null) {
            return;
        }

        Pair<String, String> pair = configSettingsForm.getPair();

        runConfiguration.setEnv(pair.getFirst());
        runConfiguration.setHttpFilePath(pair.getSecond());
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        ConfigSettingsForm configSettingsForm = new ConfigSettingsForm(myEnv, myHttpFilePath, myProject);
        myConfigSettingsForm = configSettingsForm;
        return configSettingsForm.getComponent();
    }
}
