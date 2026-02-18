package org.javamaster.httpclient.impl.completion.provider;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.impl.completion.support.HttpHeadersDictionary;
import org.javamaster.httpclient.impl.completion.support.HttpSuffixInsertHandler;
import org.javamaster.httpclient.impl.doc.support.HttpHeaderDocumentation;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequest;

/**
 * @author yudong
 */
public class HttpHeaderFieldNamesProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        HttpRequest request = PsiTreeUtil.getParentOfType(parameters.getPosition(), HttpRequest.class);
        HttpMethod method = request != null ? request.getMethod() : null;
        String methodText = method != null ? method.getText() : null;

        if (HttpRequestEnum.DUBBO.name().equals(methodText)) {
            for (String headerName : HttpHeadersDictionary.getDubboHeaderNames()) {
                LookupElementBuilder builder = LookupElementBuilder.create(headerName)
                    .withCaseSensitivity(false)
                    .withInsertHandler(HttpSuffixInsertHandler.FIELD_SEPARATOR);
                result.addElement(builder);
            }
            return;
        }

        for (HttpHeaderDocumentation header : HttpHeadersDictionary.getHeaderMap().values()) {
            var priority = PrioritizedLookupElement.withPriority(
                LookupElementBuilder.create(header, header.getName())
                    .withCaseSensitivity(false)
                    .withStrikeoutness(header.isDeprecated())
                    .withInsertHandler(HttpSuffixInsertHandler.FIELD_SEPARATOR),
                header.isDeprecated() ? 100.0 : 200.0
            );
            result.addElement(priority);
        }
    }
}
