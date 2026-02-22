package org.javamaster.httpclient.impl.action.dashboard;

import consulo.localize.LocalizeValue;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.codeEditor.Editor;
import consulo.ui.ex.awt.ClientProperty;
import consulo.ui.ex.awt.UIExAWTDataKey;
import consulo.ui.image.Image;
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

    public DashboardBaseAction(LocalizeValue text, Image icon) {
        super(text, text, icon);
    }

    protected Editor getHttpEditor(AnActionEvent e) {
        JComponent component = (JComponent) e.getData(UIExAWTDataKey.CONTEXT_COMPONENT);

        Boolean req = ClientProperty.get(component, httpDashboardToolbarKey);

        if (Boolean.TRUE.equals(req)) {
            return ClientProperty.get(component, httpDashboardReqEditorKey);
        } else {
            return ClientProperty.get(component, httpDashboardResEditorKey);
        }
    }

    protected boolean isReq(AnActionEvent e) {
        JComponent component = (JComponent) e.getData(UIExAWTDataKey.CONTEXT_COMPONENT);

        return Boolean.TRUE.equals(ClientProperty.get(component, httpDashboardToolbarKey));
    }
}
