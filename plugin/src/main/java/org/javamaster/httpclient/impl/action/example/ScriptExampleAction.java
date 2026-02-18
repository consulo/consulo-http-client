package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ScriptExampleAction extends ExampleAction {
    public ScriptExampleAction() {
        super(NlsBundle.message("request.with.tests.and.scripts"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/requests-with-scripts.http");
    }
}
