package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddEnvFileAction extends AddAction {
    public AddEnvFileAction() {
        super(NlsBundle.message("create.env.json.file"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        createAndReInitEnvCompo(false);
    }
}
