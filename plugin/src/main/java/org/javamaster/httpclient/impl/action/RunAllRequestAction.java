package org.javamaster.httpclient.impl.action;

import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.project.Project;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.handler.RunFileHandler;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class RunAllRequestAction extends AnAction {
    public RunAllRequestAction() {
        super(NlsBundle.message("run.all.tooltip"), null, HttpIcons.RUN_ALL);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        HttpEditorTopForm topForm = HttpEditorTopForm.getSelectedEditorTopForm(project);

        if (presentation.getIcon() == HttpIcons.STOP) {
            switchRunBtnToInitialing(presentation);

            RunFileHandler.stopRunning();
        } else {
            switchRunBtnToStopping(presentation);

            RunFileHandler.runRequests(project, topForm, () -> {
                switchRunBtnToInitialing(presentation);
            });
        }
    }

    private void switchRunBtnToInitialing(Presentation presentation) {
        presentation.setDescription(NlsBundle.message("run.all.tooltip"));
        presentation.setIcon(HttpIcons.RUN_ALL);
    }

    private void switchRunBtnToStopping(Presentation presentation) {
        presentation.setDescription(NlsBundle.message("stop.running"));
        presentation.setIcon(HttpIcons.STOP);
    }
}
