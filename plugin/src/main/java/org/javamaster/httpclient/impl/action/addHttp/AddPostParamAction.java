package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddPostParamAction extends AddAction {
    public AddPostParamAction() {
        super(NlsBundle.message("post.param.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("ptrp");
    }
}
