package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class PostExampleAction extends ExampleAction {
    public PostExampleAction() {
        super(NlsBundle.message("post.requests"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("examples/post-requests.http");
    }
}
