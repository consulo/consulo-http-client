package org.javamaster.httpclient.impl.psi.impl;

import consulo.language.impl.ast.FileElement;
import org.javamaster.httpclient.impl.psi.MyHttpTypes;
import org.javamaster.httpclient.psi.HttpQuery;
import org.javamaster.httpclient.psi.HttpTypes;

/**
 * @author yudong
 */
public class UrlEncodedLazyFileElement extends FileElement {
    private final CharSequence buffer;

    public UrlEncodedLazyFileElement(CharSequence buffer) {
        super(MyHttpTypes.URL_ENCODED_FILE, buffer);
        this.buffer = buffer;
    }

    public CharSequence getBuffer() {
        return buffer;
    }

    public static HttpQuery parse(String value) {
        UrlEncodedLazyFileElement fileElement = new UrlEncodedLazyFileElement(value);
        try {
            return (HttpQuery) HttpTypes.Factory.createElement(fileElement.getFirstChildNode().getFirstChildNode());
        } catch (Error e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
