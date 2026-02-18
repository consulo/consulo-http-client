package org.javamaster.httpclient.impl.handler;

import consulo.execution.runner.ProgramRunner;
import consulo.application.Application;
import consulo.language.psi.PsiUtilCore;
import consulo.project.Project;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.project.ui.wm.ToolWindowId;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.impl.dashboard.HttpProgramRunner;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 */
public class RunFileHandler {
    private static Runnable finishCallback;
    private static boolean interruptFlag = false;

    private RunFileHandler() {
    }

    public static boolean isInterrupted() {
        return interruptFlag;
    }

    public static void resetInterrupt() {
        interruptFlag = false;
    }

    public static void stopRunning() {
        interruptFlag = true;
    }

    public static void runRequests(Project project, HttpEditorTopForm topForm, Runnable finishCallback) {
        RunFileHandler.finishCallback = finishCallback;
        interruptFlag = false;

        HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, topForm.getFile());
        Collection<HttpMethod> httpMethods = PsiTreeUtil.findChildrenOfType(httpFile, HttpMethod.class);

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES);
        if (toolWindow != null) {
            toolWindow.show();
        }

        ProgramRunner<?> runner = Application.get().getExtensionPoint(ProgramRunner.class).findExtensionOrFail(HttpProgramRunner.class);
        HttpProgramRunner httpProgramRunner = (HttpProgramRunner) runner;

        Application.get().executeOnPooledThread(() -> {
            HttpMethod lastMethod = null;
            for (HttpMethod method : httpMethods) {
                lastMethod = method;
            }

            for (HttpMethod it : httpMethods) {
                it.putUserData(HttpUtils.requestFinishedKey, null);

                final HttpMethod currentMethod = it;
                Application.get().invokeLater(() -> {
                    if (!currentMethod.isValid()) {
                        NotifyUtil.notifyCornerError(project, NlsBundle.message("psi.invalid"));
                        return;
                    }

                    if (currentMethod.getText().equals(HttpRequestEnum.WEBSOCKET.name())) {
                        NotifyUtil.notifyCornerError(project, NlsBundle.message("skip.req"));
                        return;
                    }

                    if (httpProgramRunner != null) {
                        httpProgramRunner.executeFromGutter(currentMethod, null);
                    }
                });

                Integer code = it.getUserData(HttpUtils.requestFinishedKey);
                while (code == null && !interruptFlag) {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    code = it.getUserData(HttpUtils.requestFinishedKey);
                }

                if (interruptFlag) {
                    break;
                }

                if (Objects.equals(HttpUtils.FAILED, code)) {
                    break;
                }

                if (it != lastMethod) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            final Runnable callback = RunFileHandler.finishCallback;
            Application.get().invokeLater(() -> callback.run());
        });
    }
}
