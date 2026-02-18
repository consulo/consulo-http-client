package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class WsExampleAction extends ExampleAction {
    public WsExampleAction() {
        super(NlsBundle.message("websocket.requests"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/ws-requests.http");
    }
}
