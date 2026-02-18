package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddWsAction extends AddAction {
    public AddWsAction() {
        super(NlsBundle.message("ws.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("wsr");
    }
}
