package org.javamaster.httpclient;

import consulo.language.lexer.FlexAdapter;

/**
 * @author VISTALL
 * @since 2025-08-04
 */
public class HttpAdapter extends FlexAdapter {
    public HttpAdapter() {
        super(new _HttpLexer(null));
    }
}
