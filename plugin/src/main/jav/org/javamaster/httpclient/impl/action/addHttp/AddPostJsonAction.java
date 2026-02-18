package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddPostJsonAction extends AddAction {
    public AddPostJsonAction() {
        super(NlsBundle.message("post.json.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("ptr");
    }
}
