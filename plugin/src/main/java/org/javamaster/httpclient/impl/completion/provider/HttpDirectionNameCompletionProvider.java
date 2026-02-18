package org.javamaster.httpclient.impl.completion.provider;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.psi.HttpRequestBlock;

import java.util.List;

/**
 * @author yudong
 */
public class HttpDirectionNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        HttpRequestBlock requestBlock = PsiTreeUtil.getParentOfType(parameters.getPosition(), HttpRequestBlock.class);

        List<ParamEnum> params = requestBlock != null
            ? ParamEnum.getRequestParams()
            : ParamEnum.getGlobalParams();

        for (ParamEnum param : params) {
            LookupElementBuilder builder = LookupElementBuilder.create(param.getParam())
                .withTypeText(param.getDesc(), true)
                .withInsertHandler(param.insertHandler());
            result.addElement(builder);
        }
    }
}
