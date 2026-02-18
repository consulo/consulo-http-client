package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class PresentationAction extends ExampleAction {
    public PresentationAction() {
        super(NlsBundle.message("response.presentations"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/responses-presentation.http");
    }
}
