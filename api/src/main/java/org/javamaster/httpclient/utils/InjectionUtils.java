package org.javamaster.httpclient.utils;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;

public class InjectionUtils {

    public static TextRange innerRange(PsiElement context) {
        TextRange textRange = context.getTextRange();
        TextRange textRangeTmp = textRange.shiftLeft(textRange.getStartOffset());
        if (textRangeTmp.getEndOffset() == 0) {
            return null;
        }

        return textRangeTmp;
    }
}
