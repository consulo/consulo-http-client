package org.javamaster.httpclient.highlighting.support;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;

/**
 * @author VISTALL
 * @since 2025-07-22
 */
@ExtensionImpl
public class HttpSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
    @Nonnull
    @Override
    protected SyntaxHighlighter createHighlighter() {
        return new HttpSyntaxHighlighter();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
