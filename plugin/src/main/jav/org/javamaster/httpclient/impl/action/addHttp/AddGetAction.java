package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddGetAction extends AddAction {
    public AddGetAction() {
        super(NlsBundle.message("get.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("gtr");
    }
}
