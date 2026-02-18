package org.javamaster.httpclient.impl.action.dashboard;

import consulo.ui.ex.action.AnActionEvent;
import consulo.codeEditor.CaretModel;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ScrollToTopAction extends DashboardBaseAction {
    public ScrollToTopAction() {
        super(NlsBundle.message("scroll.to.top"), HttpIcons.SCROLL_UP);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getHttpEditor(e);

        CaretModel caret = editor.getCaretModel();
        caret.moveToOffset(0);

        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }
}
