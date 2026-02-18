package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AuthExampleAction extends ExampleAction {
    public AuthExampleAction() {
        super(NlsBundle.message("request.with.authorization"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/requests-with-authorization.http");
    }
}
