package org.javamaster.httpclient.impl.annotator;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.document.util.TextRange;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpVariableArg;

/**
 * @author yudong
 */
public class VariableAnnotator {

    public static void annotateVariableName(boolean builtin, TextRange range, AnnotationHolder holder) {
        if (range.getStartOffset() == range.getEndOffset()) {
            return;
        }

        String tip = builtin 
                ? NlsBundle.message("builtin.variable")
                : NlsBundle.message("user.defined.variable");

        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(range)
                .tooltip(tip)
                .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                .create();
    }

    public static void annotateVariableArg(HttpVariableArg variableArg, TextRange range, AnnotationHolder holder) {
        if (range.getStartOffset() == range.getEndOffset()) {
            return;
        }

        var textAttributes = variableArg.getString() != null
                ? DefaultLanguageHighlighterColors.STRING
                : DefaultLanguageHighlighterColors.NUMBER;

        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(range)
                .textAttributes(textAttributes)
                .create();
    }

    public static void annotateQueryName(TextRange range, AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(range)
                .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD)
                .create();
    }

    public static void annotateQueryValue(TextRange range, AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(range)
                .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                .create();
    }

    public static void annotateRequestName(TextRange range, AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(new TextRange(range.getStartOffset(), range.getStartOffset() + 3))
                .textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
                .create();
    }
}
