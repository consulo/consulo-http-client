package org.javamaster.httpclient.impl.action;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.popup.PopupFactoryImpl;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ShowAddHttpPopupAction extends AnAction {
    public ShowAddHttpPopupAction() {
        super(NlsBundle.message("add.to.http"), null, PlatformIconGroup.generalAdd);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ActionManager actionManager = ActionManager.getInstance();
        PopupFactoryImpl popupFactory = PopupFactoryImpl.getInstance();

        ActionGroup group = (ActionGroup) actionManager.getAction("addToHttpGroup");

        var listPopup = popupFactory.createActionGroupPopup(
                NlsBundle.message("new"), group, e.getDataContext(),
                true, null, 16
        );

        listPopup.showUnderneathOf(e.getInputEvent().getComponent());
    }
}
