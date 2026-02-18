package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class GetExampleAction extends ExampleAction {
    public GetExampleAction() {
        super(NlsBundle.message("get.requests"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/get-requests.http");
    }
}
