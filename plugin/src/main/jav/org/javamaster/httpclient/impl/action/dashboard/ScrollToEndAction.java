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
public class ScrollToEndAction extends DashboardBaseAction {
    public ScrollToEndAction() {
        super(NlsBundle.message("scroll.to.end"), HttpIcons.SCROLL_DOWN);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = getHttpEditor(e);

        CaretModel caret = editor.getCaretModel();
        caret.moveToOffset(editor.getDocument().getTextLength());

        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }
}
