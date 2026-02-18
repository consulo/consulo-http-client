package org.javamaster.httpclient.psi;

import consulo.language.ast.IElementType;
import org.javamaster.httpclient.HttpLanguage;
import org.jetbrains.annotations.NonNls;

/**
 * @author yudong
 */
public class HttpTokenType extends IElementType {
    public HttpTokenType(@NonNls String debugName) {
        super(debugName, HttpLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return HttpTokenType.class.getSimpleName() + "." + super.toString();
    }
}
