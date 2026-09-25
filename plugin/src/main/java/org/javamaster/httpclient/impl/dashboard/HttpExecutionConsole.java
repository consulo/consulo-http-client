package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.ui.ExecutionConsole;
import consulo.ui.Component;
import consulo.ui.ex.awtUnsafe.TargetAWT;

import javax.swing.*;

public class HttpExecutionConsole implements ExecutionConsole {
    private final Component myComponent;

    public HttpExecutionConsole(Component component) {
        myComponent = component;
    }

    @Override
    public void dispose() {

    }

    @Override
    public Component getUIComponent() {
        return myComponent;
    }

    @Override
    public Component getUIPreferredFocusableComponent() {
        return myComponent;
    }

    @Override
    public JComponent getComponent() {
        return (JComponent) TargetAWT.to(myComponent);
    }

    @Override
    public JComponent getPreferredFocusableComponent() {
        return getComponent();
    }
}
