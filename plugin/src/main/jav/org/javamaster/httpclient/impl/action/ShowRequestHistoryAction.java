package org.javamaster.httpclient.impl.action;

import consulo.ui.ex.action.ActionUpdateThread;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.application.WriteAction;
import consulo.fileEditor.FileEditorManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.impl.curl.CurlParser;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.javamaster.httpclient.impl.utils.VirtualFileUtils;

import java.io.File;
import java.util.Arrays;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.application.EDTKt.runInEdt;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ShowRequestHistoryAction extends AnAction {
    public ShowRequestHistoryAction() {
        super(NlsBundle.message("show.req.history"), null, HttpIcons.HISTORY);
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(VirtualFile.KEY);

        if (HttpUtils.isHistoryFile(virtualFile)) {
            e.getPresentation().setEnabled(false);
            return;
        }

        HttpRequestBlock requestBlock = ConvertToCurlAndCpAction.findRequestBlock(e);

        e.getPresentation().setEnabled(requestBlock != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        HttpRequestBlock requestBlock = ConvertToCurlAndCpAction.findRequestBlock(e);
        if (requestBlock == null) return;

        HttpRequest request = requestBlock.getRequest();
        Project project = e.getProject();

        String method = request.getMethod().getText();
        if (method.equals(HttpRequestEnum.WEBSOCKET.name())
                || method.equals(HttpRequestEnum.DUBBO.name())) {
            NotifyUtil.notifyWarn(project, NlsBundle.message("convert.not.supported"));
            return;
        }

        String tabName = HttpUtils.getTabName(request.getMethod());

        File dateHistoryDir = VirtualFileUtils.getDateHistoryDir(project);
        File bodyFilesFolder = new File(dateHistoryDir, tabName);

        File[] listFiles = bodyFilesFolder.listFiles();
        if (listFiles == null) {
            NotifyUtil.notifyWarn(project, NlsBundle.message("no.res.body.files"));
            return;
        }

        try {
            CurlParser.toCurlString(requestBlock, project, true, it -> {
                getApplication().executeOnPooledThread(() -> {
                    String historyBodyFileStrList = Arrays.stream(listFiles)
                            .map(historyBodyFile -> "<> " + tabName + "/" + historyBodyFile.getName())
                            .limit(30)
                            .reduce((a, b) -> a + HttpUtils.CR_LF + b)
                            .orElse("");

                    String content = it + HttpUtils.CR_LF + historyBodyFileStrList;

                    runInEdt(() -> {
                        try {
                            WriteAction.run(() -> {
                                VirtualFile virtualFile = VirtualFileUtils.createHistoryHttpVirtualFile(content, project, tabName);

                                FileEditorManager editorManager = FileEditorManager.getInstance(project);
                                editorManager.openFile(virtualFile);
                            });
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                });
            });
        } catch (Exception e2) {
            NotifyUtil.notifyError(project, e2.toString());
        }
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
