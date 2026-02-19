package org.javamaster.httpclient.impl.dashboard;

import consulo.process.NopProcessHandler;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;

import javax.swing.*;
import java.io.OutputStream;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
public class HttpProcessHandler extends NopProcessHandler {
    public final HttpMethod httpMethod;
    public final String selectedEnv;
    public final String tabName;
    public final Project project;

    public HttpProcessHandler(HttpMethod httpMethod, String selectedEnv) {
        this.httpMethod = httpMethod;
        this.selectedEnv = selectedEnv;
        this.tabName = HttpUtilsPart.getTabName(httpMethod);
        this.project = httpMethod.getProject();
    }

    public JComponent getComponent() {
        return new JLabel("test");//TODO !
    }

    @Override
    protected void destroyProcessImpl() {

    }

    @Override
    protected void detachProcessImpl() {

    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return null;
    }
}
