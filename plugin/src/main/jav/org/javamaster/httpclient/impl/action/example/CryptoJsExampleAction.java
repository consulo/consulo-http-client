package org.javamaster.httpclient.impl.action.example;

import consulo.ui.ex.action.AnActionEvent;
import org.javamaster.httpclient.NlsBundle;

@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class CryptoJsExampleAction extends ExampleAction {
    public CryptoJsExampleAction() {
        super(NlsBundle.message("show.cryptojs.file"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        openExample("js/crypto-js.js");
    }
}
