package org.javamaster.httpclient.impl.action;

import consulo.ui.ex.action.ActionManager;
import consulo.dataContext.DataContext;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.ComboBoxAction;
import org.javamaster.httpclient.NlsBundle;

import javax.swing.*;
import java.awt.*;

/**
 * @author yudong
 */
public class ShowExamplePopupAction extends ComboBoxAction {
    public ShowExamplePopupAction() {
        getTemplatePresentation().setText(NlsBundle.message("http.examples"));
        getTemplatePresentation().setDescription(NlsBundle.message("http.example.desc"));
    }

    @Override
    protected ComboBoxButton createComboBoxButton(Presentation presentation) {
        ComboBoxButton button = super.createComboBoxButton(presentation);

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(null);

        return button;
    }

    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent button, DataContext dataContext) {
        ActionManager actionManager = ActionManager.getInstance();
        return (DefaultActionGroup) actionManager.getAction("exampleHttpGroup");
    }
}
