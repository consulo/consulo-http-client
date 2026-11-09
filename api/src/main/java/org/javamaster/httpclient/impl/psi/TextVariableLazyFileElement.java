package org.javamaster.httpclient.impl.psi;

import consulo.language.impl.ast.FileElement;
import org.javamaster.httpclient.psi.HttpMyJsonValue;
import org.javamaster.httpclient.psi.impl.HttpTypesFactory;

/**
 * @author yudong
 */
public class TextVariableLazyFileElement extends FileElement {
    private final CharSequence buffer;

    public TextVariableLazyFileElement(CharSequence buffer) {
        super(MyHttpTypes.TEXT_VARIABLE_FILE, buffer);
        this.buffer = buffer;
    }

    public CharSequence getBuffer() {
        return buffer;
    }

    public static HttpMyJsonValue parse(String value) {
        TextVariableLazyFileElement fileElement = new TextVariableLazyFileElement(value);
        return (HttpMyJsonValue) HttpTypesFactory.createElement(fileElement.getFirstChildNode());
    }
}
