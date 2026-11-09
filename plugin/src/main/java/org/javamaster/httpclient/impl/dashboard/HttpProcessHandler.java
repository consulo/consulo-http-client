package org.javamaster.httpclient.impl.dashboard;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.http.HttpRequestBuilderFactory;
import consulo.httpClient.execution.HttpFileContentReader;
import consulo.httpClient.execution.env.HttpEnvironment;
import consulo.httpClient.execution.runner.HttpRequestRunResult;
import consulo.httpClient.execution.runner.HttpRequestRunner;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.localize.LocalizeValue;
import consulo.process.BaseProcessHandler;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.wm.ToolWindowId;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFileManager;
import org.javamaster.httpclient.impl.ui.HttpDashboardForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.VirtualFileUtils;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;

/**
 * Runs one request with {@link HttpRequestRunner} and shows the result in the Services dashboard.
 *
 * @author yudong
 */
public class HttpProcessHandler extends BaseProcessHandler {
    /**
     * Reads referenced files, preferring unsaved editor content.
     */
    private static final HttpFileContentReader EDITOR_CONTENT_READER = new HttpFileContentReader() {
        @Override
        public byte[] readBytes(File file) throws IOException {
            return VirtualFileUtils.readNewestBytes(file);
        }

        @Override
        public String readText(File file) throws IOException {
            return VirtualFileUtils.readNewestContent(file);
        }
    };

    private final String selectedEnv;
    public final LocalizeValue tabName;
    public final Project project;
    public Integer httpStatus;
    public Long costTimes;

    private final HttpMethod httpMethod;
    private final String parentPath;
    private final Runnable loadingRemover;

    private HttpDashboardForm httpDashboardForm;

    public boolean hasError = false;

    private Image myIcon;

    @RequiredReadAction
    public HttpProcessHandler(HttpMethod httpMethod, String selectedEnv) {
        this.httpMethod = httpMethod;
        this.selectedEnv = selectedEnv;
        this.tabName = HttpUtilsPart.getTabName(httpMethod);
        this.project = httpMethod.getProject();
        this.parentPath = httpMethod.getContainingFile().getVirtualFile().getParent().getPath();
        this.loadingRemover = httpMethod.getUserData(HttpUtils.gutterIconLoadingKey);

        myIcon = HttpUtils.pickMethodIcon(httpMethod.getText());
    }

    public Image getIcon() {
        return myIcon;
    }

    @RequiredUIAccess
    public Component getUIComponent() {
        if (httpDashboardForm == null) {
            httpDashboardForm = new HttpDashboardForm(tabName, project);
        }
        return httpDashboardForm.getUIComponent();
    }

    @Override
    public void startNotify() {
        super.startNotify();

        Application application = Application.get();
        application.executeOnPooledThread(() -> {
            HttpRequestRunResult result = runRequest(application);

            // the dashboard writes the response into a history file - needs a write action, like before
            application.invokeLater(() -> application.runWriteAction(() -> {
                try {
                    httpStatus = result.statusCode();
                    costTimes = result.costMillis();

                    if (result.savedFile() != null) {
                        VirtualFileManager.getInstance().asyncRefresh(null);
                    }

                    dealResponse(result.httpInfo(), result.paramMap().containsKey(ParamEnum.NO_LOG.getParam()));
                }
                finally {
                    destroyProcess();
                }
            }));
        });
    }

    private HttpRequestRunResult runRequest(Application application) {
        HttpEnvironment environment;
        try {
            environment = HttpEnvironment.load(Path.of(parentPath), selectedEnv);
        }
        catch (IOException e) {
            environment = HttpEnvironment.EMPTY;
        }

        HttpRequestBuilderFactory requestBuilderFactory = application.getInstance(HttpRequestBuilderFactory.class);

        return new HttpRequestRunner(project, requestBuilderFactory, EDITOR_CONTENT_READER).run(httpMethod, selectedEnv, environment);
    }

    @RequiredUIAccess
    private void dealResponse(HttpInfo httpInfo, boolean noLog) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        if (httpDashboardForm == null) {
            httpDashboardForm = new HttpDashboardForm(tabName, project);
        }
        httpDashboardForm.initHttpResContent(httpInfo, noLog);

        Throwable myThrowable = httpDashboardForm.getThrowable();
        hasError = myThrowable != null;
        if (hasError) {
            LocalizeValue error = (myThrowable instanceof CancellationException || myThrowable.getCause() instanceof CancellationException)
                ? HttpClientLocalize.reqInterrupted(tabName)
                : HttpClientLocalize.reqFailed(tabName, myThrowable);
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, NotificationType.ERROR, error.get());
        }
        else {
            String msg = tabName + " " + HttpClientLocalize.requestSuccess();
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, NotificationType.SUCCESS, msg);
        }
    }

    @Override
    protected void destroyProcessImpl() {
        if (loadingRemover != null) {
            Application.get().invokeLater(loadingRemover::run);
        }

        int code = hasError ? HttpUtils.FAILED : HttpUtils.SUCCESS;

        notifyProcessTerminated(code);
    }

    @Override
    protected void detachProcessImpl() {
        destroyProcessImpl();
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return true;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return null;
    }
}
