package org.javamaster.httpclient.impl.ui;

import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.HttpFileType;
import org.javamaster.httpclient.env.EnvFileService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigSettingsForm {
    private final String myNoEnv = HttpClientLocalize.noEnv().get();

    private final ComboBox<String> myEnvComboBox;
    private final FileChooserTextBoxBuilder.Controller myHttpFileBox;
    private final Component myComponent;

    @RequiredUIAccess
    public ConfigSettingsForm(String env, String httpFilePath, Project project) {
        File parentFile = new File(httpFilePath).getParentFile();
        Set<String> presetEnvSet = EnvFileService.getService(project).getPresetEnvSet(parentFile == null ? null : parentFile.getAbsolutePath());

        List<String> envNameList = new ArrayList<>();
        envNameList.add(myNoEnv);
        envNameList.addAll(presetEnvSet);

        myEnvComboBox = ComboBox.create(envNameList);
        myEnvComboBox.setValue(StringUtil.isEmpty(env) ? myNoEnv : env);

        FileChooserTextBoxBuilder httpFileBuilder = FileChooserTextBoxBuilder.create(project);
        httpFileBuilder.fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileDescriptor(HttpFileType.INSTANCE));
        myHttpFileBox = httpFileBuilder.build();
        myHttpFileBox.setValue(StringUtil.notNullize(httpFilePath));

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(HttpClientLocalize.env(), myEnvComboBox);
        builder.addLabeled(HttpClientLocalize.httpFile(), myHttpFileBox.getComponent());
        myComponent = builder.build();
    }

    public Pair<String, String> getPair() {
        String env = StringUtil.notNullize(myEnvComboBox.getValue());
        if (env.equals(myNoEnv)) {
            env = "";
        }

        String fileName = StringUtil.notNullize(myHttpFileBox.getValue()).replace('\\', '/');

        return new Pair<>(env, fileName);
    }

    public Component getComponent() {
        return myComponent;
    }
}
