package org.javamaster.httpclient.impl.action.dashboard;

import consulo.codeEditor.Editor;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.ActionButton;
import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.HttpIcons;

/**
 * @author yudong
 */
public class ShowLineNumberAction extends DashboardBaseAction /*implements CustomComponentAction*/ {
    public static boolean reqShowLineNum = true;
    public static boolean resShowLineNum = true;

    private final Editor editor;
    private final boolean req;

    public ShowLineNumberAction(Editor editor, boolean req) {
        super(HttpClientLocalize.showLineNum(), null);
        this.editor = editor;
        this.req = req;
    }

    // TODO !
//    @Override
//    public JComponent createCustomComponent(Presentation presentation, String place) {
//        ActionButtonWithText actionButtonWithText = new ActionButtonWithText(this, presentation, place, new Dimension(20, 20));
//
//        boolean showLineNum = getShowLineNum();
//
//        setShowLineNum(showLineNum, actionButtonWithText);
//
//        return actionButtonWithText;
//    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionButton actionButtonWithText = (ActionButton) e.getInputEvent().getComponent();

        boolean showLineNum = switchShowLineNum();

        setShowLineNum(showLineNum, actionButtonWithText);
    }

    private void setShowLineNum(boolean showLineNum, ActionButton actionButtonWithText) {
        editor.getSettings().setLineNumbersShown(showLineNum);

        if (showLineNum) {
            actionButtonWithText.getPresentation().setIcon(PlatformIconGroup.actionsChecked());
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
