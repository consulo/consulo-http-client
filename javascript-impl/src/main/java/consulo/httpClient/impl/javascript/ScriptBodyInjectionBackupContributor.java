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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
@ExtensionImpl
public class ScriptBodyInjectionBackupContributor implements MultiHostInjector {
    /**
     * Declarations of client, request, response and helpers - invisible prefix of the injected script, so the
     * JavaScript plugin resolves and completes them.
     */
    private static final String API_DECLARATIONS = loadApiDeclarations();

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
        registrar.addPlace(API_DECLARATIONS, null, (PsiLanguageInjectionHost) context, textRange);
        registrar.doneInjecting();
    }

    private static String loadApiDeclarations() {
        try (InputStream stream = ScriptBodyInjectionBackupContributor.class.getResourceAsStream("http-client-api.js")) {
            if (stream == null) {
                throw new IllegalStateException("http-client-api.js not found");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
