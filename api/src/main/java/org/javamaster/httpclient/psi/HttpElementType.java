package org.javamaster.httpclient.psi;

import consulo.language.ast.IElementType;
import org.javamaster.httpclient.HttpLanguage;
import org.jetbrains.annotations.NonNls;

/**
 * @author yudong
 */
public class HttpElementType extends IElementType {
    public HttpElementType(@NonNls String debugName) {
        super(debugName, HttpLanguage.INSTANCE);
    }
}
