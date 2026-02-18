package org.javamaster.httpclient.parser;

import consulo.language.lexer.FlexAdapter;
import org.javamaster.httpclient._HttpLexer;

/**
 * @author yudong
 */
public class HttpAdapter extends FlexAdapter {

    public HttpAdapter() {
        super(new _HttpLexer(null));
    }
}
