package org.javamaster.httpclient.impl.action.dashboard;

import consulo.codeEditor.Editor;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.HttpIcons;

/**
 * @author yudong
 */
public class ViewSettingsAction extends DashboardBaseAction {
    // TODO private final ContentTypeActionGroup contentTypeActionGroup;

    public ViewSettingsAction(Editor editor) {
        super(HttpClientLocalize.viewSettings(), HttpIcons.INSPECTIONS_EYE);
        // this.contentTypeActionGroup = new ContentTypeActionGroup(editor);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO !
        //        Editor editor = getHttpEditor(e);
//
//        DefaultActionGroup actionGroup = new DefaultActionGroup();
//
//        boolean req = isReq(e);
//
//        ShowLineNumberAction showLineNumberAction = new ShowLineNumberAction(editor, req);
//        actionGroup.add(showLineNumberAction);
//
//        FoldHeadersAction foldHeadersAction = new FoldHeadersAction(editor, req);
//        actionGroup.add(foldHeadersAction);
//
//        actionGroup.addSeparator("View As");
//
//        actionGroup.addAll(contentTypeActionGroup.getActions());
//
//        PopupFactoryImpl jbPopupFactory = PopupFactoryImpl.getInstance();
//        ActionManager actionManager = ActionManager.getInstance();
//
//        var actionToolbar = actionManager.createActionToolbar("httpViewSettingsToolBar", actionGroup, false);
//        actionToolbar.setShowSeparatorTitles(true);
//
//        jbPopupFactory.createComponentPopupBuilder(actionToolbar.getComponent(), null)
//                .createPopup()
//                .showUnderneathOf(e.getInputEvent().getComponent());
    }
}
