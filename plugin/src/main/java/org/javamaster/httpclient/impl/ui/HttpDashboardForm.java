package org.javamaster.httpclient.impl.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.disposer.Disposable;
import consulo.httpClient.impl.internal.action.HttpDashboardVerticalGroup;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.file.light.LightVirtualFile;
import consulo.project.Project;
import consulo.ui.ex.action.*;
import consulo.ui.ex.awt.JBScrollPane;
import consulo.ui.ex.awt.JBSplitter;
import consulo.ui.ex.awt.JBUI;
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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpDashboardForm implements Disposable {
    private final static Map<String, HttpDashboardForm> historyMap = Maps.newHashMap();

    private final List<Editor> editorList = Lists.newArrayList();
    public JPanel mainPanel;
    public Throwable throwable;
    public JPanel requestPanel;
    public JPanel responsePanel;
    private JPanel reqVerticalToolbarPanel;
    private JPanel resVerticalToolbarPanel;
    @SuppressWarnings("unused")
    private JPanel reqPanel;
    @SuppressWarnings("unused")
    private JPanel resPanel;
    private JBSplitter splitter;

    private final String tabName;
    private final Project project;

    public HttpDashboardForm(String tabName, Project project) {
        this.tabName = tabName;
        this.project = project;

        init();

        splitter.setSplitterProportionKey("httpRequestCustomProportionKey");

        disposePreviousReqEditors();

        historyMap.put(tabName, this);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void initHttpResContent(HttpInfo httpInfo, boolean noLog) {
        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        throwable = httpInfo.getHttpException();
        SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

        byte[] reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);

        Editor reqEditor = HttpUiUtils.createEditor(reqBytes, "req.http", project, tabName,
            editorList, true, simpleTypeEnum, noLog);

        requestPanel.add(reqEditor.getComponent(), constraints);

        // TODO  initVerticalToolbarPanel(reqEditor, reqVerticalToolbarPanel, null, null);

        if (throwable != null) {
            String msg = ExceptionUtils.getStackTrace(throwable);

            Editor errorEditor = HttpUiUtils.createEditor(msg.getBytes(StandardCharsets.UTF_8),
                "error.log", project, tabName, editorList, false, simpleTypeEnum, noLog);

            responsePanel.add(errorEditor.getComponent(), constraints);

            // TODO initVerticalToolbarPanel(errorEditor, resVerticalToolbarPanel, null, null);

            return;
        }

        VirtualFile responseBodyFile = saveResponseToFile(httpInfo, tabName, noLog);

        byte[] resBytes = String.join("", httpInfo.getHttpResDescList()).getBytes(StandardCharsets.UTF_8);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        Editor resEditor = HttpUiUtils.createEditor(resBytes, "res.http", project, tabName,
            editorList, false, simpleTypeEnum, noLog);

        responsePanel.add(resEditor.getComponent(), constraintsRes);

        initVerticalToolbarPanel(resEditor, resVerticalToolbarPanel, simpleTypeEnum, responseBodyFile);
//
//        if (Objects.equals(simpleTypeEnum, SimpleTypeEnum.IMAGE)) {
//            ImageEditorImpl imageEditor = new ImageEditorImpl(project, responseBodyFile);
//
//            JBScrollPane presentation = new JBScrollPane(imageEditor.getComponent());
//
//            renderResponsePresentation(resEditor.getComponent(), presentation, constraintsRes);
//        }
    }

    private void initVerticalToolbarPanel(Editor target, JPanel jPanel, SimpleTypeEnum resType, VirtualFile resBodyFile) {
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
        toolbar.setTargetComponent(target.getComponent());

        JComponent component = toolbar.getComponent();

        jPanel.add(component);
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

            File dateHistoryDir = VirtualFileUtils.getDateHistoryDir(project);

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

    private void renderResponsePresentation(JComponent resComponent, JComponent presentation, GridConstraints constraintsRes) {
        Dimension size = resComponent.getSize();
        resComponent.setPreferredSize(new Dimension(size.width, 160));

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(resComponent, BorderLayout.NORTH);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(new JLabel(HttpClientLocalize.resRenderResult().get()), BorderLayout.NORTH);
        previewPanel.add(presentation, BorderLayout.CENTER);

        jPanel.add(previewPanel, BorderLayout.CENTER);

        responsePanel.add(new JBScrollPane(jPanel), constraintsRes);
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
        HttpDashboardForm previousHttpDashboardForm = historyMap.remove(tabName);
        if (previousHttpDashboardForm == null) {
            return;
        }

        previousHttpDashboardForm.disposeEditors();
    }

    private void disposeEditors() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);
        });
    }

    @Override
    public void dispose() {

    }

    private void init() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        splitter = new JBSplitter();
        splitter.setLayout(new BorderLayout(0, 0));
        mainPanel.add(splitter, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        reqPanel = new JPanel();
        reqPanel.setLayout(new GridLayoutManager(1, 2, JBUI.emptyInsets(), -1, -1));
        splitter.add(reqPanel, BorderLayout.WEST);
        requestPanel = new JPanel();
        requestPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        reqPanel.add(requestPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        reqVerticalToolbarPanel = new JPanel();
        reqVerticalToolbarPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        reqPanel.add(reqVerticalToolbarPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(20, -1), new Dimension(20, -1), new Dimension(20, -1), 0, false));
        resPanel = new JPanel();
        resPanel.setLayout(new GridLayoutManager(1, 3, JBUI.emptyInsets(), -1, -1));
        splitter.add(resPanel, BorderLayout.EAST);
        responsePanel = new JPanel();
        responsePanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        resPanel.add(responsePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        resVerticalToolbarPanel = new JPanel();
        resVerticalToolbarPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        resPanel.add(resVerticalToolbarPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(20, -1), new Dimension(20, -1), new Dimension(20, -1), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("");
        resPanel.add(label1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        splitter.setSecondComponent(resPanel);
        splitter.setFirstComponent(reqPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
