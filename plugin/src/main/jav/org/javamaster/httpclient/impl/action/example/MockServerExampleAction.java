package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class MockServerExampleAction extends ExampleAction {
    public MockServerExampleAction() {
        super("Mock Server");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/mock-server.http");
    }
}
