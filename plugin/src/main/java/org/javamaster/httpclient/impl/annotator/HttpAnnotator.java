package org.javamaster.httpclient.impl.annotator;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import org.javamaster.httpclient.psi.HttpComment;
import org.javamaster.httpclient.psi.HttpVariableName;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof HttpVariableName) {
            HttpVariableName variableName = (HttpVariableName) element;
            VariableAnnotator.annotateVariableName(variableName.isBuiltin(), element.getTextRange(), holder);
        } else if (element instanceof HttpComment) {
            VariableAnnotator.annotateRequestName(element.getTextRange(), holder);
        }
    }
}
