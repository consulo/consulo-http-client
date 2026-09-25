package org.javamaster.httpclient.impl.ui;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.disposer.Disposable;
import consulo.httpClient.impl.internal.action.HttpDashboardVerticalGroup;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.file.light.LightVirtualFile;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.Space;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.*;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.SplitLayoutPosition;
import consulo.ui.layout.TwoComponentSplitLayout;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.javamaster.httpclient.impl.action.dashboard.PreviewFileAction;
import org.javamaster.httpclient.impl.action.dashboard.SoftWrapAction;
import org.javamaster.httpclient.impl.action.dashboard.ViewSettingsAction;
import org.javamaster.httpclient.impl.key.HttpKey;
import org.javamaster.httpclient.impl.utils.HttpUiUtils;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.VirtualFileUtils;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class HttpDashboardForm implements Disposable {
    private static final Map<String, HttpDashboardForm> ourHistoryMap = new HashMap<>();

    private final List<Editor> myEditorList = new ArrayList<>();

    private final String myTabName;
    private final Project myProject;

    private final TwoComponentSplitLayout myRootLayout;
    private final DockLayout myRequestLayout;
    private final DockLayout myResponseLayout;

    private @Nullable Throwable myThrowable;

    @RequiredUIAccess
    public HttpDashboardForm(String tabName, Project project) {
        myTabName = tabName;
        myProject = project;

        myRequestLayout = DockLayout.create(Space.NONE);
        myResponseLayout = DockLayout.create(Space.NONE);

        myRootLayout = TwoComponentSplitLayout.create(SplitLayoutPosition.HORIZONTAL)
            .withFirstComponent(myRequestLayout)
            .withSecondComponent(myResponseLayout);

        disposePreviousReqEditors();

        ourHistoryMap.put(tabName, this);
    }

    public Component getUIComponent() {
        return myRootLayout;
    }

    public @Nullable Throwable getThrowable() {
        return myThrowable;
    }

    @RequiredUIAccess
    public void initHttpResContent(HttpInfo httpInfo, boolean noLog) {
        myThrowable = httpInfo.getHttpException();
        SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

        byte[] reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);

        Editor reqEditor = HttpUiUtils.createEditor(reqBytes, "req.http", myProject, myTabName,
            myEditorList, true, simpleTypeEnum, noLog);

        myRequestLayout.center(reqEditor.getUIComponent());

        // TODO initVerticalToolbar(reqEditor, myRequestLayout, null, null);

        if (myThrowable != null) {
            String msg = ExceptionUtils.getStackTrace(myThrowable);

            Editor errorEditor = HttpUiUtils.createEditor(msg.getBytes(StandardCharsets.UTF_8),
                "error.log", myProject, myTabName, myEditorList, false, simpleTypeEnum, noLog);

            myResponseLayout.center(errorEditor.getUIComponent());

            // TODO initVerticalToolbar(errorEditor, myResponseLayout, null, null);
        }
        else {
            VirtualFile responseBodyFile = saveResponseToFile(httpInfo, myTabName, noLog);

            byte[] resBytes = String.join("", httpInfo.getHttpResDescList()).getBytes(StandardCharsets.UTF_8);

            Editor resEditor = HttpUiUtils.createEditor(resBytes, "res.http", myProject, myTabName,
                myEditorList, false, simpleTypeEnum, noLog);

            myResponseLayout.center(resEditor.getUIComponent());

            initVerticalToolbar(resEditor, myResponseLayout, simpleTypeEnum, responseBodyFile);
//
//            if (Objects.equals(simpleTypeEnum, SimpleTypeEnum.IMAGE)) {
//                ImageEditorImpl imageEditor = new ImageEditorImpl(myProject, responseBodyFile);
//
//                renderResponsePresentation(resEditor.getUIComponent(), ScrollableLayout.create(imageEditor.getUIComponent()));
//            }
        }

        // the form is already showing when the response arrives - without it the editors may stay unpainted
        myRootLayout.forceRepaint();
    }

    @RequiredUIAccess
    private void initVerticalToolbar(Editor target, DockLayout layout, SimpleTypeEnum resType, VirtualFile resBodyFile) {
        ActionManager actionManager = ActionManager.getInstance();

        AnAction viewSettingsAction = new ViewSettingsAction(target);
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup(viewSettingsAction, new SoftWrapAction(target));

        ActionGroup actionGroup = actionManager.getAction(HttpDashboardVerticalGroup.class);
        defaultActionGroup.addAll(actionGroup);

        if (Objects.equals(resType, SimpleTypeEnum.HTML) || Objects.equals(resType, SimpleTypeEnum.PDF)) {
            resBodyFile.putUserData(HttpKey.httpDashboardBinaryBodyKey, true);

            defaultActionGroup.add(new PreviewFileAction(resBodyFile));
        }
        else if (Objects.equals(resType, SimpleTypeEnum.IMAGE)) {
            defaultActionGroup.add(new PreviewFileAction(resBodyFile));
        }

        ActionToolbar toolbar = actionManager.createActionToolbar("httpDashboardVerticalToolbar", defaultActionGroup, false);
        toolbar.setTargetUIComponent(target.getUIComponent());

        layout.right(toolbar.getUIComponent());
    }

    private VirtualFile saveResponseToFile(HttpInfo httpInfo, String tabName, boolean noLog) {
        try {
            SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

            String contentType = httpInfo.getContentType();

            //noinspection DataFlowIssue
            String suffix = SimpleTypeEnum.getSuffix(simpleTypeEnum, contentType);

            String fileName = DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HHmmss") + "." + suffix;

            if (noLog) {
                LightVirtualFile lightVirtualFile = new LightVirtualFile(fileName);
                lightVirtualFile.setCharset(StandardCharsets.UTF_8);
                //noinspection DataFlowIssue
                lightVirtualFile.setBinaryContent(httpInfo.getByteArray());
                return lightVirtualFile;
            }

            File dateHistoryDir = VirtualFileUtils.getDateHistoryDir(myProject);

            File resBodyDir = new File(dateHistoryDir, tabName);
            if (!resBodyDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                resBodyDir.mkdirs();
            }

            File file = new File(resBodyDir, fileName);

            String absolutePath = file.getAbsolutePath();

            boolean deleted = file.delete();
            if (deleted) {
                //System.out.println("已删除文件:" + absolutePath);
            }

            //noinspection DataFlowIssue
            Files.write(file.toPath(), httpInfo.getByteArray());
            //System.out.println("已保存到文件:" + absolutePath);

            VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, true);

            httpInfo.getHttpResDescList().add(HttpUtils.CR_LF + ">> " + absolutePath + HttpUtils.CR_LF);

            return virtualFile;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredUIAccess
    private void renderResponsePresentation(Component resComponent, Component presentation) {
        DockLayout resLayout = DockLayout.create(Space.NONE);
        resLayout.center(resComponent);
        resLayout.setHeight(160);

        DockLayout previewLayout = DockLayout.create();
        previewLayout.top(Label.create(HttpClientLocalize.resRenderResult()));
        previewLayout.center(presentation);

        DockLayout layout = DockLayout.create();
        layout.top(resLayout);
        layout.center(previewLayout);

        myResponseLayout.center(ScrollableLayout.create(layout));
    }

//    public void initWsForm(WsRequest wsRequest) {
//        reqPanel.remove(reqVerticalToolbarPanel);
//        resPanel.remove(resVerticalToolbarPanel);
//
//        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
//        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);
//        constraints = (GridConstraints) constraints.clone();
//        int width = 200;
//        constraints.myMinimumSize.width = width;
//        constraints.myMaximumSize.width = width;
//        constraints.myPreferredSize.width = width;
//
//        JPanel jPanelReq = createReqPanel(wsRequest);
//
//        requestPanel.add(jPanelReq, constraints);
//
//        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
//        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);
//
//        Editor editor = WriteAction.computeAndWait(() ->
//            HttpUiUtils.createEditor("".getBytes(StandardCharsets.UTF_8), "ws.log",
//                project, tabName, editorList, false)
//        );
//
//        responsePanel.add(editor.getComponent(), constraintsRes);
//
//        wsRequest.setResConsumer(res ->
//            DocumentUtil.writeInRunUndoTransparentAction(() -> {
//                    String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
//                    String replace = res.replace(HttpUtils.CR_LF, "\n");
//                    String s = time + " - " + replace;
//
//                    Document document = editor.getDocument();
//                    document.insertString(document.getTextLength(), s);
//
//                    Caret caret = editor.getCaretModel().getPrimaryCaret();
//                    caret.moveToOffset(document.getTextLength());
//
//                    ScrollingModel scrollingModel = editor.getScrollingModel();
//                    scrollingModel.scrollToCaret(ScrollType.RELATIVE);
//                }
//            )
//        );
//    }

//    public void initMockServerForm(MockServer mockServer) {
//        mainPanel.remove(splitter);
//        mainPanel.setLayout(new BorderLayout());
//
//        Editor editor = WriteAction.computeAndWait(() ->
//            HttpUiUtils.INSTANCE.createEditor("".getBytes(StandardCharsets.UTF_8), "mockServer.log",
//                project, tabName, editorList, false)
//        );
//
//        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);
//
//        mockServer.setResConsumer(res ->
//            ApplicationManager.getApplication().invokeLater(() ->
//                DocumentUtil.writeInRunUndoTransparentAction(() -> {
//                        Document document = editor.getDocument();
//                        document.insertString(document.getTextLength(), res);
//
//                        Caret caret = editor.getCaretModel().getPrimaryCaret();
//                        caret.moveToOffset(document.getTextLength());
//
//                        ScrollingModel scrollingModel = editor.getScrollingModel();
//                        scrollingModel.scrollToCaret(ScrollType.RELATIVE);
//                    }
//                ))
//        );
//    }

//    private static @NotNull JPanel createReqPanel(WsRequest wsRequest) {
//        JPanel jPanelReq = new JPanel();
//        jPanelReq.setLayout(new BorderLayout());
//
//        JTextArea jTextAreaReq = new JTextArea();
//        jTextAreaReq.setToolTipText(NlsBundle.INSTANCE.message("ws.tooltip"));
//        jPanelReq.add(new JBScrollPane(jTextAreaReq), BorderLayout.CENTER);
//
//        JButton jButtonSend = new JButton(NlsBundle.INSTANCE.message("ws.send"));
//        jButtonSend.addActionListener(e -> {
//            String text = jTextAreaReq.getText();
//            wsRequest.sendWsMsg(text);
//            jTextAreaReq.setText("");
//        });
//
//        JPanel btnPanel = new JPanel();
//        btnPanel.add(jButtonSend);
//
//        jPanelReq.add(btnPanel, BorderLayout.SOUTH);
//        return jPanelReq;
//    }

    private void disposePreviousReqEditors() {
        HttpDashboardForm previousHttpDashboardForm = ourHistoryMap.remove(myTabName);
        if (previousHttpDashboardForm == null) {
            return;
        }

        previousHttpDashboardForm.disposeEditors();
    }

    private void disposeEditors() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        myEditorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);
        });
    }

    @Override
    public void dispose() {

    }
}
