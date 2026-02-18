package org.javamaster.httpclient.impl.completion.provider;

import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.module.Module;
import consulo.module.ModuleUtil;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiClass;
import consulo.language.psi.PsiMethod;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.impl.completion.support.HttpHeadersDictionary;
import org.javamaster.httpclient.psi.HttpHeader;
import org.javamaster.httpclient.psi.HttpHeaderField;
import org.javamaster.httpclient.psi.HttpHeaderFieldValue;
import org.javamaster.httpclient.impl.utils.DubboUtils;
import org.javamaster.httpclient.impl.utils.HttpUtils;

import java.util.List;

/**
 * @author yudong
 */
public class HttpHeaderFieldValuesProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        HttpHeaderField headerField = PsiTreeUtil.getParentOfType(
            CompletionUtil.getOriginalOrSelf(parameters.getPosition()),
            HttpHeaderField.class
        );
        String headerName = headerField != null && headerField.getHeaderFieldName() != null
            ? headerField.getHeaderFieldName().getText()
            : null;
        if (StringUtil.isEmpty(headerName)) {
            return;
        }

        List<String> headerValues = HttpHeadersDictionary.getHeaderValuesMap().get(headerName);
        if (headerValues != null) {
            for (String value : headerValues) {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(value), 200.0));
            }
            return;
        }

        if (headerName.equalsIgnoreCase(DubboUtils.INTERFACE_KEY)) {
            CompletionResultSet newResult = result.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));
            JavaClassNameCompletionContributor.addAllClasses(
                parameters,
                parameters.getInvocationCount() <= 1,
                newResult.getPrefixMatcher(),
                newResult
            );
            return;
        }

        if (headerName.equalsIgnoreCase(DubboUtils.METHOD_KEY)) {
            HttpHeader header = (HttpHeader) headerField.getParent();
            HttpHeaderField interfaceField = header.getInterfaceField();
            if (interfaceField == null) {
                return;
            }

            HttpHeaderFieldValue fieldValue = interfaceField.getHeaderFieldValue();
            if (fieldValue == null) {
                return;
            }

            Module module = ModuleUtil.findModuleForPsiElement(header);
            if (module == null) {
                return;
            }

            PsiClass interfacePsiClass = DubboUtils.findInterface(module, fieldValue.getText());
            if (interfacePsiClass == null) {
                return;
            }

            for (PsiMethod method : interfacePsiClass.getMethods()) {
                String desc = HttpUtils.getMethodDesc(method);
                String returnType = method.getReturnTypeElement() != null
                    ? method.getReturnTypeElement().getText()
                    : "";
                LookupElementBuilder builder = LookupElementBuilder.create(method.getName())
                    .withBoldness(true)
                    .withPsiElement(method)
                    .withTailText(method.getParameterList().getText())
                    .withTypeText(returnType + " " + desc);
                result.addElement(builder);
            }
        }
    }
}
