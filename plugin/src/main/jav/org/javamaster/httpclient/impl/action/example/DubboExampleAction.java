package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class DubboExampleAction extends ExampleAction {
    public DubboExampleAction() {
        super(NlsBundle.message("dubbo.requests"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/dubbo-requests.http");
    }
}
