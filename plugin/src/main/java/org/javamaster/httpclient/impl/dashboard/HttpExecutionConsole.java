package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.ui.ExecutionConsole;

import javax.swing.*;

public class HttpExecutionConsole implements ExecutionConsole {
    private final JComponent myComponent;

    public HttpExecutionConsole(JComponent myComponent) {
        this.myComponent = myComponent;
    }

    @Override
    public void dispose() {

    }

    @Override
    public JComponent getComponent() {
        return myComponent;
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return myComponent;
    }
}
