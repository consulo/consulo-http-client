package org.javamaster.httpclient.impl.action.dashboard;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.CustomComponentAction;
import consulo.ui.ex.action.ActionButton;
import consulo.codeEditor.Editor;
import consulo.ui.ex.JBColor;
import org.javamaster.httpclient.NlsBundle;

import javax.swing.*;
import java.awt.*;

/**
 * @author yudong
 */
public class SoftWrapAction extends DashboardBaseAction implements CustomComponentAction {
    private final Editor editor;
    public static boolean reqUseSoftWrap = false;
    public static boolean resUseSoftWrap = false;

    public SoftWrapAction(Editor editor) {
        super(NlsBundle.message("soft.wrap"), PlatformIconGroup.actionsToggleSoftWrap);
        this.editor = editor;
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        ActionButton actionButton = new ActionButton(this, presentation, place, new Dimension(20, 20));

        Boolean req = editor.getComponent().getUserData(httpDashboardToolbarKey);

        boolean useSoftWrap;
        if (req) {
            useSoftWrap = reqUseSoftWrap;
        } else {
            useSoftWrap = resUseSoftWrap;
        }

        if (useSoftWrap) {
            actionButton.setBackground(JBColor.LIGHT_GRAY);
        } else {
            actionButton.setBackground(null);
        }

        return actionButton;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionButton actionButton = (ActionButton) e.getInputEvent().getComponent();

        boolean useSoftWrap;
        if (isReq(e)) {
            reqUseSoftWrap = !reqUseSoftWrap;
            useSoftWrap = reqUseSoftWrap;
        } else {
            resUseSoftWrap = !resUseSoftWrap;
            useSoftWrap = resUseSoftWrap;
        }

        setSoftWrap(useSoftWrap, actionButton);
    }

    private void setSoftWrap(boolean useSoftWrap, ActionButton actionButton) {
        var settings = editor.getSettings();
        settings.setUseSoftWraps(useSoftWrap);
        if (useSoftWrap) {
            actionButton.setBackground(JBColor.LIGHT_GRAY);
        } else {
            actionButton.setBackground(null);
        }
    }
}
