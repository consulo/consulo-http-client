package org.javamaster.httpclient.impl.ui;

import com.google.common.collect.Lists;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.project.Project;
import consulo.ui.ex.awt.CollectionComboBoxModel;
import consulo.ui.ex.awt.TextBrowseFolderListener;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.util.lang.Pair;
import org.javamaster.httpclient.impl.action.ChooseEnvironmentAction;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;

public class ConfigSettingsForm {
    private JPanel mainPanel;

    private JComboBox<String> envComboBox;
    private TextFieldWithBrowseButton httpFileBtn;

    private final String noEnv = ChooseEnvironmentAction.Companion.getNoEnv();

    public void initForm(String env, String httpFilePath, @NotNull Project project) {
        File httpFile = new File(httpFilePath);

        initEnvComboBox(env, httpFile, project);

        initFileBtn(httpFilePath);
    }

    public Pair<String, String> getPair() {
        String env = envComboBox.getSelectedItem() + "";
        if (env.equals(noEnv)) {
            env = "";
        }

        String fileName = httpFileBtn.getText();

        return new Pair<>(env, fileName);
    }

    private void initEnvComboBox(String env, File httpFile, Project project) {
        EnvFileService envFileService = EnvFileService.Companion.getService(project);

        Set<String> presetEnvSet = envFileService.getPresetEnvSet(httpFile.getParentFile().getAbsolutePath());

        List<String> envNameList = Lists.newArrayList(noEnv);
        envNameList.addAll(presetEnvSet);

        envComboBox.setModel(new CollectionComboBoxModel<>(envNameList));

        envComboBox.setSelectedItem(env);
    }

    private void initFileBtn(String httpFilePath) {
        httpFileBtn.setText(httpFilePath);

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();

        httpFileBtn.addBrowseFolderListener(new TextBrowseFolderListener(descriptor) {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);

                httpFileBtn.setText(httpFileBtn.getText().replaceAll("\\\\", "/"));
            }

        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
