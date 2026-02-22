package org.javamaster.httpclient.impl.action;

import consulo.dataContext.DataContext;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.editor.DaemonCodeAnalyzer;
import consulo.language.editor.PlatformDataKeys;
import consulo.project.Project;
import consulo.ui.ex.action.*;
import consulo.ui.ex.awt.action.ComboBoxAction;
import consulo.ui.ex.awt.action.ComboBoxButtonImpl;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.env.EnvFileService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public class ChooseEnvironmentAction extends ComboBoxAction {
    private final VirtualFile file;
    private ComboBoxButtonImpl comboBoxButton;
    private String selectedEnv;

    public static final String noEnv = HttpClientLocalize.noEnv().get();
    private Presentation myPresentation;

    public ChooseEnvironmentAction(VirtualFile file) {
        this.file = file;
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        ComboBoxButtonImpl button = createComboBoxButton(presentation);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel jLabel = new JLabel(HttpClientLocalize.env().get());
        jLabel.setPreferredSize(new Dimension(38, jLabel.getPreferredSize().height));
        jLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(jLabel, BorderLayout.WEST);

        panel.add(button, BorderLayout.CENTER);

        return panel;
    }

    @Nonnull
    @Override
    protected ComboBoxButtonImpl createComboBoxButton(Presentation presentation) {
        myPresentation = presentation;

        presentation.setDescriptionValue(HttpClientLocalize.envTooltip());

        presentation.setText(selectedEnv != null ? selectedEnv : noEnv);

        comboBoxButton = (ComboBoxButtonImpl) super.createComboBoxButton(presentation);
        comboBoxButton.setBorder(null);

        return comboBoxButton;
    }

    @Nonnull
    @Override
    protected ActionGroup createPopupActionGroup(JComponent jComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ActionGroup createPopupActionGroup(JComponent button, DataContext dataContext) {
        var project = dataContext.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return ActionGroup.EMPTY_GROUP;
        }

        String path = file.getParent() != null ? file.getParent().getPath() : null;
        if (path == null) {
            return ActionGroup.EMPTY_GROUP;
        }

        EnvFileService envFileService = EnvFileService.getService(project);
        Set<String> presetEnvSet = envFileService.getPresetEnvSet(path);

        List<String> envList = new ArrayList<>();
        envList.add(noEnv);
        envList.addAll(presetEnvSet);

        List<AnAction> actions = new ArrayList<>();
        for (String env : envList) {
            actions.add(new MyAction(env));
        }

        return new DefaultActionGroup(actions);
    }

    public String getSelectedEnv() {
        return selectedEnv;
    }

    public void setSelectEnv(String env) {
        if (env.isEmpty() || env.equals(noEnv)) {
            selectedEnv = null;
        } else {
            selectedEnv = env;
        }

        if (comboBoxButton != null) {
            myPresentation.setText(selectedEnv != null ? selectedEnv : noEnv);
        }
    }

    private class MyAction extends AnAction {
        private final String env;

        MyAction(String env) {
            super(env);
            this.env = env;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            setSelectEnv(env);

            DaemonCodeAnalyzer.getInstance(e.getData(Project.KEY)).restart();
        }
    }
}
