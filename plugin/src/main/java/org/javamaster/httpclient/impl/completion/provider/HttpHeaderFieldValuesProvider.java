package org.javamaster.httpclient.impl.completion.provider;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.impl.completion.support.HttpHeadersDictionary;
import org.javamaster.httpclient.psi.HttpHeaderField;

import java.util.List;

/**
 * @author yudong
 */
public class HttpHeaderFieldValuesProvider implements CompletionProvider {

    @RequiredReadAction
    @Override
    public void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        HttpHeaderField headerField = PsiTreeUtil.getParentOfType(
            CompletionUtilCore.getOriginalOrSelf(parameters.getPosition()),
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

//        if (headerName.equalsIgnoreCase(DubboUtils.INTERFACE_KEY)) {
//            CompletionResultSet newResult = result.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));
//            JavaClassNameCompletionContributor.addAllClasses(
//                parameters,
//                parameters.getInvocationCount() <= 1,
//                newResult.getPrefixMatcher(),
//                newResult
//            );
//            return;
//        }

//        if (headerName.equalsIgnoreCase(DubboUtils.METHOD_KEY)) {
//            HttpHeader header = (HttpHeader) headerField.getParent();
//            HttpHeaderField interfaceField = header.getInterfaceField();
//            if (interfaceField == null) {
//                return;
//            }
//
//            HttpHeaderFieldValue fieldValue = interfaceField.getHeaderFieldValue();
//            if (fieldValue == null) {
//                return;
//            }
//
//            Module module = ModuleUtil.findModuleForPsiElement(header);
//            if (module == null) {
//                return;
//            }
//
//            PsiClass interfacePsiClass = DubboUtils.findInterface(module, fieldValue.getText());
//            if (interfacePsiClass == null) {
//                return;
//            }
//
//            for (PsiMethod method : interfacePsiClass.getMethods()) {
//                String desc = HttpUtils.getMethodDesc(method);
//                String returnType = method.getReturnTypeElement() != null
//                    ? method.getReturnTypeElement().getText()
//                    : "";
//                LookupElementBuilder builder = LookupElementBuilder.create(method.getName())
//                    .withBoldness(true)
//                    .withPsiElement(method)
//                    .withTailText(method.getParameterList().getText())
//                    .withTypeText(returnType + " " + desc);
//                result.addElement(builder);
//            }
//        }
    }
}
