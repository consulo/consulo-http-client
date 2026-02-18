package org.javamaster.httpclient.impl.manipulator;

import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import org.javamaster.httpclient.psi.HttpRequestTarget;

public class HttpRequestTargetManipulator extends AbstractElementManipulator<HttpRequestTarget> {

    @Override
    public HttpRequestTarget handleContentChange(HttpRequestTarget element, TextRange range, String newContent) {
        return element;
    }

}
