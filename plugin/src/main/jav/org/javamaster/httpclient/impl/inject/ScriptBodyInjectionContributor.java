package org.javamaster.httpclient.impl.inject;

import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.util.collection.SmartList;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.javamaster.httpclient.utils.InjectionUtils;
import org.jetbrains.annotations.NotNull;
import ris58h.webcalm.javascript.JavaScriptLanguage;

import java.util.List;

/**
 * @author yudong
 */
public class ScriptBodyInjectionContributor implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (!WebCalm.isAvailable()) {
            return;
        }

        TextRange textRange = InjectionUtils.innerRange(context);
        if (textRange == null) {
            return;
        }

        try {
            registrar.startInjecting(JavaScriptLanguage.INSTANCE);
            registrar.addPlace(null, null, (PsiLanguageInjectionHost) context, textRange);
            registrar.doneInjecting();
        } catch (Error e) {
            System.out.println(e.getMessage());
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return new SmartList<>(HttpScriptBody.class);
    }
}
