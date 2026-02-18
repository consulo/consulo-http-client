package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddDubboNameAction extends AddAction {
    public AddDubboNameAction() {
        super(NlsBundle.message("dubbo.req.name"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("dtrp");
    }
}
