package org.javamaster.httpclient.impl.dashboard;

import consulo.application.ReadAction;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.execution.event.ExecutionListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.localize.LocalizeValue;
import consulo.process.BaseProcessHandler;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessOutputTypes;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Runs the requests one by one, each as the run of its own configuration. The next request is started after the think time,
 * when the previous one is finished without an error.
 *
 * @author VISTALL
 * @since 2026-09-25
 */
public class HttpRunAllProcessHandler extends BaseProcessHandler {
    private static final int THINK_TIME_SECONDS = 2;

    private static final Set<String> UNSUPPORTED_METHODS = Set.of(HttpRequestEnum.WEBSOCKET.name(), HttpRequestEnum.MOCK_SERVER.name());

    private final Project myProject;
    private final HttpProgramRunner myRunner;
    private final List<SmartPsiElementPointer<HttpMethod>> myHttpMethods;
    private final String myEnv;
    private final Disposable myListenerDisposable = Disposable.newDisposable();

    private int myIndex;
    private @Nullable String myRunningTabName;
    private volatile @Nullable ExecutionEnvironment myRunningEnvironment;
    private volatile @Nullable ProcessHandler myRunningProcessHandler;
    private volatile boolean myStopped;

    public HttpRunAllProcessHandler(
        Project project,
        HttpProgramRunner runner,
        List<SmartPsiElementPointer<HttpMethod>> httpMethods,
        String env
    ) {
        myProject = project;
        myRunner = runner;
        myHttpMethods = httpMethods;
        myEnv = env;
    }

    @Override
    public void startNotify() {
        super.startNotify();

        UIAccess uiAccess = myProject.getUIAccess();

        myProject.getMessageBus().connect(myListenerDisposable).subscribe(ExecutionListener.class, new ExecutionListener() {
            @Override
            public void processNotStarted(String executorId, ExecutionEnvironment env) {
                if (env == myRunningEnvironment) {
                    uiAccess.give(() -> requestFinished(false));
                }
            }

            @Override
            public void processStarted(String executorId, ExecutionEnvironment env, ProcessHandler handler) {
                if (env == myRunningEnvironment) {
                    myRunningProcessHandler = handler;
                }
            }

            @Override
            public void processTerminated(String executorId, ExecutionEnvironment env, ProcessHandler handler, int exitCode) {
                if (env == myRunningEnvironment) {
                    uiAccess.give(() -> requestFinished(exitCode == HttpUtils.SUCCESS));
                }
            }
        });

        uiAccess.give(this::runNext);
    }

    @RequiredUIAccess
    private void runNext() {
        while (!myStopped && myIndex < myHttpMethods.size()) {
            SmartPsiElementPointer<HttpMethod> pointer = myHttpMethods.get(myIndex++);

            HttpMethod httpMethod = ReadAction.compute(pointer::getElement);
            if (httpMethod == null) {
                // the request was removed from the file after the start
                continue;
            }

            String tabName = ReadAction.compute(() -> HttpUtilsPart.getTabName(httpMethod));

            String methodName = ReadAction.compute(httpMethod::getText);
            if (UNSUPPORTED_METHODS.contains(methodName)) {
                print(HttpClientLocalize.runAllSkipped(tabName, methodName));
                continue;
            }

            print(HttpClientLocalize.runAllStarted(tabName));

            // the reason why the request can't be run is notified already
            ExecutionEnvironment environment = myRunner.createEnvironment(httpMethod, myEnv);
            if (environment == null) {
                fail(tabName);
                return;
            }

            myRunningTabName = tabName;
            myRunningEnvironment = environment;
            try {
                myRunner.execute(environment);
            }
            catch (ExecutionException e) {
                myRunningEnvironment = null;
                fail(tabName);
            }
            return;
        }

        if (!myStopped) {
            print(HttpClientLocalize.runAllDone());
            finish(HttpUtils.SUCCESS);
        }
    }

    @RequiredUIAccess
    private void requestFinished(boolean success) {
        String tabName = myRunningTabName;

        myRunningTabName = null;
        myRunningEnvironment = null;
        myRunningProcessHandler = null;

        if (myStopped) {
            return;
        }

        if (!success) {
            fail(tabName);
            return;
        }

        if (myIndex >= myHttpMethods.size()) {
            runNext();
            return;
        }

        UIAccess uiAccess = myProject.getUIAccess();
        AppExecutorUtil.getAppScheduledExecutorService()
            .schedule(() -> uiAccess.give(this::runNext), THINK_TIME_SECONDS, TimeUnit.SECONDS);
    }

    private void fail(@Nullable String tabName) {
        print(HttpClientLocalize.runAllFailed(tabName == null ? "" : tabName));
        finish(HttpUtils.FAILED);
    }

    private void finish(int exitCode) {
        Disposer.dispose(myListenerDisposable);

        notifyProcessTerminated(exitCode);
    }

    private void print(LocalizeValue text) {
        notifyTextAvailable(text.get() + "\n", ProcessOutputTypes.SYSTEM);
    }

    private void stop() {
        if (myStopped) {
            return;
        }

        myStopped = true;

        Disposer.dispose(myListenerDisposable);

        ProcessHandler runningProcessHandler = myRunningProcessHandler;
        if (runningProcessHandler != null && !runningProcessHandler.isProcessTerminated()) {
            runningProcessHandler.destroyProcess();
        }

        print(HttpClientLocalize.runAllStopped());
    }

    @Override
    protected void destroyProcessImpl() {
        stop();

        notifyProcessTerminated(HttpUtils.FAILED);
    }

    @Override
    protected void detachProcessImpl() {
        stop();

        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return null;
    }
}
