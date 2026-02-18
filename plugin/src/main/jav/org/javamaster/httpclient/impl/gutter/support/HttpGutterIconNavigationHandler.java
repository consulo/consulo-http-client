package org.javamaster.httpclient.impl.gutter.support;

import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.execution.runner.ProgramRunner;
import consulo.codeEditor.EditorGutterComponentEx;
import consulo.language.psi.PsiElement;
import org.javamaster.httpclient.impl.dashboard.HttpProgramRunner;
import org.javamaster.httpclient.psi.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

import static org.javamaster.httpclient.impl.dashboard.HttpProgramRunner.HTTP_RUNNER_ID;

/**
 * @author yudong
 */
public class HttpGutterIconNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
    public static final HttpGutterIconNavigationHandler INSTANCE = new HttpGutterIconNavigationHandler();

    private HttpGutterIconNavigationHandler() {
    }

    @Override
    public void navigate(@NotNull MouseEvent event, @NotNull PsiElement element) {
        EditorGutterComponentEx gutterComponent = (EditorGutterComponentEx) event.getComponent();

        HttpProgramRunner httpProgramRunner = (HttpProgramRunner) ProgramRunner.findRunnerById(HTTP_RUNNER_ID);
        if (httpProgramRunner == null) {
            return;
        }

        httpProgramRunner.executeFromGutter((HttpMethod) element.getParent(), gutterComponent);
    }
}
