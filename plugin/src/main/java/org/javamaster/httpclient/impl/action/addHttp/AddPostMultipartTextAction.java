package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddPostMultipartTextAction extends AddAction {
    public AddPostMultipartTextAction() {
        super(NlsBundle.message("post.multi.text.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("mptr");
    }
}
