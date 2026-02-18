package org.javamaster.httpclient.impl.manipulator;

import consulo.document.util.TextRange;
import consulo.language.psi.ElementManipulator;
import org.javamaster.httpclient.psi.HttpFilePath;

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
}
