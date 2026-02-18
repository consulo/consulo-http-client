package org.javamaster.httpclient.impl.action.dashboard.view;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.CustomComponentAction;
import consulo.ui.ex.action.ActionButtonWithText;
import consulo.codeEditor.Editor;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.action.dashboard.DashboardBaseAction;
import org.javamaster.httpclient.NlsBundle;

import javax.swing.*;
import java.awt.*;

/**
 * @author yudong
 */
public class ShowLineNumberAction extends DashboardBaseAction implements CustomComponentAction {
    public static boolean reqShowLineNum = true;
    public static boolean resShowLineNum = true;

    private final Editor editor;
    private final boolean req;

    public ShowLineNumberAction(Editor editor, boolean req) {
        super(NlsBundle.message("show.line.num"), null);
        this.editor = editor;
        this.req = req;
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation, String place) {
        ActionButtonWithText actionButtonWithText = new ActionButtonWithText(this, presentation, place, new Dimension(20, 20));

        boolean showLineNum = getShowLineNum();

        setShowLineNum(showLineNum, actionButtonWithText);

        return actionButtonWithText;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionButtonWithText actionButtonWithText = (ActionButtonWithText) e.getInputEvent().getComponent();

        boolean showLineNum = switchShowLineNum();

        setShowLineNum(showLineNum, actionButtonWithText);
    }

    private void setShowLineNum(boolean showLineNum, ActionButtonWithText actionButtonWithText) {
        editor.getSettings().setLineNumbersShown(showLineNum);

        if (showLineNum) {
            actionButtonWithText.getPresentation().setIcon(PlatformIconGroup.actionsChecked);
        } else {
            actionButtonWithText.getPresentation().setIcon(HttpIcons.BLANK);
        }
    }

    private boolean getShowLineNum() {
        if (req) {
            return reqShowLineNum;
        } else {
            return resShowLineNum;
        }
    }

    private boolean switchShowLineNum() {
        if (req) {
            reqShowLineNum = !reqShowLineNum;
            return reqShowLineNum;
        } else {
            resShowLineNum = !resShowLineNum;
            return resShowLineNum;
        }
    }
}
