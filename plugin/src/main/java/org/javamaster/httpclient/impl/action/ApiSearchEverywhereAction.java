package org.javamaster.httpclient.impl.action;

import consulo.ide.impl.idea.ide.actions.SearchEverywhereBaseAction;
import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.symbol.ApisGotoSEContributor;

/**
 * @author yudong
 */
public class ApiSearchEverywhereAction extends SearchEverywhereBaseAction {
    public ApiSearchEverywhereAction() {
        getTemplatePresentation().setText(NlsBundle.message("search.api"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        String tabID = ApisGotoSEContributor.class.getSimpleName();
        showInSearchEverywherePopup(tabID, e, true, false);
    }
}
