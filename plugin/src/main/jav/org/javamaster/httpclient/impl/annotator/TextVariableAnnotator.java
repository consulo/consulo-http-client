package org.javamaster.httpclient.impl.annotator;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import org.javamaster.httpclient.impl.reference.support.QueryNamePsiReference;
import org.javamaster.httpclient.impl.reference.support.QueryValuePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableArgNamePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableNamePsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class TextVariableAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiReference[] references = element.getReferences();
        if (references.length == 0) {
            return;
        }

        for (PsiReference ref : references) {
            if (ref instanceof TextVariableNamePsiReference) {
                TextVariableNamePsiReference reference = (TextVariableNamePsiReference) ref;
                boolean builtin = reference.getVariable().getVariableName() != null 
                        && reference.getVariable().getVariableName().isBuiltin();
                VariableAnnotator.annotateVariableName(builtin, ref.getRangeInElement(), holder);
            } else if (ref instanceof TextVariableArgNamePsiReference) {
                TextVariableArgNamePsiReference reference = (TextVariableArgNamePsiReference) ref;
                VariableAnnotator.annotateVariableArg(reference.getVariableArg(), ref.getRangeInElement(), holder);
            } else if (ref instanceof QueryNamePsiReference) {
                VariableAnnotator.annotateQueryName(ref.getRangeInElement(), holder);
            } else if (ref instanceof QueryValuePsiReference) {
                VariableAnnotator.annotateQueryValue(ref.getRangeInElement(), holder);
            }
        }
    }
}
