package consulo.httpClient.impl.javascript;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.javascript.language.JavaScriptLanguage;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.javamaster.httpclient.utils.InjectionUtils;

/**
 * @author yudong
 */
@ExtensionImpl
public class ScriptBodyInjectionBackupContributor implements MultiHostInjector {

    @Nonnull
    @Override
    public Class<? extends PsiElement> getElementClass() {
        return HttpScriptBody.class;
    }

    @Override
    public void injectLanguages(@Nonnull MultiHostRegistrar registrar, @Nonnull PsiElement context) {
        TextRange textRange = InjectionUtils.innerRange(context);
        if (textRange == null) {
            return;
        }

        registrar.startInjecting(JavaScriptLanguage.INSTANCE);
        registrar.addPlace(null, null, (PsiLanguageInjectionHost) context, textRange);
        registrar.doneInjecting();
    }
}
