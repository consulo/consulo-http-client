package org.javamaster.httpclient.impl.action;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.ui.ViewVariableForm;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ShowVariableFormAction extends AnAction {
    public ShowVariableFormAction() {
        super(NlsBundle.message("show.variables"), null, PlatformIconGroup.generalInlineVariables);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ViewVariableForm viewVariableForm = new ViewVariableForm(e.getProject());
        viewVariableForm.show();
    }
}
