package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddEnvPrivateFileAction extends AddAction {
    public AddEnvPrivateFileAction() {
        super(NlsBundle.message("create.env.private.json.file"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        createAndReInitEnvCompo(true);
    }
}
