package org.javamaster.httpclient.impl.manipulator;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.psi.HttpRequestTarget;

@ExtensionImpl
public class HttpRequestTargetManipulator extends AbstractElementManipulator<HttpRequestTarget> {

    @Override
    public HttpRequestTarget handleContentChange(HttpRequestTarget element, TextRange range, String newContent) {
        return element;
    }

    @Nonnull
    @Override
    public Class<HttpRequestTarget> getElementClass() {
        return HttpRequestTarget.class;
    }

}
