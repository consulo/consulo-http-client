package org.javamaster.httpclient.impl.completion.provider;

import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.psi.HttpMethod;

/**
 * @author yudong
 */
public class HttpMethodsProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        if (!isRequestStart(parameters)) {
            return;
        }

        for (HttpRequestEnum requestEnum : HttpRequestEnum.values()) {
            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(requestEnum.name())
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 300.0
                )
            );
        }
    }

    private boolean isRequestStart(CompletionParameters parameters) {
        PsiElement parent = parameters.getPosition().getParent();
        return parent instanceof PsiErrorElement || parent instanceof HttpMethod;
    }
}
