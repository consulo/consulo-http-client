package org.javamaster.httpclient.impl.manipulator;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.ElementManipulator;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.psi.HttpFilePath;

@ExtensionImpl
public class HttpFilePathManipulator implements ElementManipulator<HttpFilePath> {
    @Override
    public HttpFilePath handleContentChange(HttpFilePath element, TextRange range, String newContent) {
        return element;
    }

    @Override
    public HttpFilePath handleContentChange(HttpFilePath element, String newContent) {
        return element;
    }

    @Override
    public TextRange getRangeInElement(HttpFilePath element) {
        return element.getTextRange();
    }

    @Nonnull
    @Override
    public Class<HttpFilePath> getElementClass() {
        return HttpFilePath.class;
    }
}
