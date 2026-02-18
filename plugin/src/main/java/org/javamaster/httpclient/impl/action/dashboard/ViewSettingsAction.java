package org.javamaster.httpclient.impl.action.dashboard;

import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.codeEditor.Editor;
import consulo.ui.ex.popup.PopupFactoryImpl;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.action.dashboard.view.ContentTypeActionGroup;
import org.javamaster.httpclient.impl.action.dashboard.view.FoldHeadersAction;
import org.javamaster.httpclient.impl.action.dashboard.view.ShowLineNumberAction;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
public class ViewSettingsAction extends DashboardBaseAction {
    private final ContentTypeActionGroup contentTypeActionGroup;

    public ViewSettingsAction(Editor editor) {
        super(NlsBundle.message("view.settings"), HttpIcons.INSPECTIONS_EYE);
        this.contentTypeActionGroup = new ContentTypeActionGroup(editor);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getHttpEditor(e);

        DefaultActionGroup actionGroup = new DefaultActionGroup();

        boolean req = isReq(e);

        ShowLineNumberAction showLineNumberAction = new ShowLineNumberAction(editor, req);
        actionGroup.add(showLineNumberAction);

        FoldHeadersAction foldHeadersAction = new FoldHeadersAction(editor, req);
        actionGroup.add(foldHeadersAction);

        actionGroup.addSeparator("View As");

        actionGroup.addAll(contentTypeActionGroup.getActions());

        PopupFactoryImpl jbPopupFactory = PopupFactoryImpl.getInstance();
        ActionManager actionManager = ActionManager.getInstance();

        var actionToolbar = actionManager.createActionToolbar("httpViewSettingsToolBar", actionGroup, false);
        actionToolbar.setShowSeparatorTitles(true);

        jbPopupFactory.createComponentPopupBuilder(actionToolbar.getComponent(), null)
                .createPopup()
                .showUnderneathOf(e.getInputEvent().getComponent());
    }
}
