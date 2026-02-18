package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddDubboAction extends AddAction {
    public AddDubboAction() {
        super(NlsBundle.message("dubbo.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("dtr");
    }
}
