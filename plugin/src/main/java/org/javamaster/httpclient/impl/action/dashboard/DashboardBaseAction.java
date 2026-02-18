package org.javamaster.httpclient.impl.action.dashboard;

import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.codeEditor.Editor;
import consulo.util.dataholder.Key;
import org.javamaster.httpclient.model.SimpleTypeEnum;

import javax.swing.*;

/**
 * @author yudong
 */
public abstract class DashboardBaseAction extends AnAction {
    public static final Key<Boolean> httpDashboardToolbarKey = Key.create("org.javamaster.dashboard.httpDashboardToolbar");
    public static final Key<SimpleTypeEnum> httpDashboardResTypeKey = Key.create("org.javamaster.dashboard.httpDashboardResType");
    public static final Key<Editor> httpDashboardReqEditorKey = Key.create("org.javamaster.dashboard.httpDashboardReqEditor");
    public static final Key<Editor> httpDashboardResEditorKey = Key.create("org.javamaster.dashboard.httpDashboardResEditor");

    public DashboardBaseAction(String text, Icon icon) {
        super(text, null, icon);
    }

    protected Editor getHttpEditor(AnActionEvent e) {
        JComponent component = (JComponent) PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());

        Boolean req = component.getUserData(httpDashboardToolbarKey);

        if (req) {
            return component.getUserData(httpDashboardReqEditorKey);
        } else {
            return component.getUserData(httpDashboardResEditorKey);
        }
    }

    protected boolean isReq(AnActionEvent e) {
        JComponent component = (JComponent) PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());

        return component.getUserData(httpDashboardToolbarKey);
    }
}
