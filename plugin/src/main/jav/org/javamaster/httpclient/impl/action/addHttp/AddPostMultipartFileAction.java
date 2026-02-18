package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class AddPostMultipartFileAction extends AddAction {
    public AddPostMultipartFileAction() {
        super(NlsBundle.message("post.multi.file.req"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        startLiveTemplate("fptr");
    }
}
