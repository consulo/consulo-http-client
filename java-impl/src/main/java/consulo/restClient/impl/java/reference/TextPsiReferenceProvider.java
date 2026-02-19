package consulo.httpClient.impl.java.reference;

import com.intellij.java.language.psi.PsiMethod;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.plain.psi.PsiPlainTextFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.impl.reference.support.QueryValuePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableArgNamePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableNamePsiReference;
import org.javamaster.httpclient.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yudong
 */
public class TextPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
        @NotNull PsiElement element,
        @NotNull ProcessingContext context
    ) {
        var plainTextFile = (PsiPlainTextFile) element;
        var project = plainTextFile.getProject();

        var injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(plainTextFile);
        if (!(injectionHost instanceof HttpMessageBody)) {
            return PsiReference.EMPTY_ARRAY;
        }

        var text = plainTextFile.getText();
        var delta = plainTextFile.getTextRange().getStartOffset();

        var parent = ((HttpMessageBody) injectionHost).getParent().getParent();

        var request = PsiTreeUtil.getParentOfType(injectionHost, HttpRequest.class);
        if (request == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        if (request.getContentType() == ContentType.APPLICATION_FORM_URLENCODED
            || (parent instanceof HttpMultipartField && ((HttpMultipartField) parent).getContentType() == ContentType.APPLICATION_FORM_URLENCODED)
        ) {
            var query = UrlEncodedLazyFileElement.parse(text);
            if (query == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            var references = request.getRequestTarget() != null ? request.getRequestTarget().getReferences() : new PsiReference[0];

            PsiMethod controllerMethod = null;
            if (references.length > 0) {
                var resolved = references[0].resolve();
                if (resolved instanceof PsiMethod) {
                    controllerMethod = (PsiMethod) resolved;
                }
            }

            return createUrlEncodedReferences(plainTextFile, (HttpMessageBody) injectionHost, query, delta, controllerMethod);
        }

        return createTextVariableReferences(plainTextFile, (HttpMessageBody) injectionHost, text, delta);
    }

    private PsiReference[] createUrlEncodedReferences(
        PsiElement psiElement,
        @Nullable HttpMessageBody messageBody,
        HttpQuery query,
        int delta,
        @Nullable PsiMethod controllerMethod
    ) {
        List<PsiReference> references = new ArrayList<>();

        for (var queryParameter : query.getQueryParameterList()) {
            var queryParameterName = queryParameter.getQueryParameterKey();
            var queryName = queryParameterName.getText();

            var nameRange = queryParameterName.getTextRange().shiftRight(delta);

            var nameReference = new QueryNamePsiReference(psiElement, nameRange, controllerMethod, queryName);
            references.add(nameReference);

            var queryParameterValue = queryParameter.getQueryParameterValue();
            if (queryParameterValue == null) {
                continue;
            }

            var variable = queryParameterValue.getVariable();
            if (variable != null) {
                var list = createVariableReferences(variable, psiElement, messageBody, delta);
                references.addAll(list);
            } else {
                var valueRange = queryParameterValue.getTextRange().shiftRight(delta);

                var valueReference = new QueryValuePsiReference(psiElement, valueRange);
                references.add(valueReference);
            }
        }

        return references.toArray(new PsiReference[0]);
    }

    public static PsiReference[] createTextVariableReferences(
        PsiElement psiElement,
        @Nullable HttpMessageBody messageBody,
        String text,
        int delta
    ) {
        var textVariable = TextVariableLazyFileElement.parse(text);

        return textVariable.getVariableList().stream()
            .flatMap(variable -> createVariableReferences(variable, psiElement, messageBody, delta).stream())
            .toArray(PsiReference[]::new);
    }

    private static List<PsiReference> createVariableReferences(
        HttpVariable variable,
        PsiElement psiElement,
        @Nullable HttpMessageBody messageBody,
        int delta
    ) {
        var variableName = variable.getVariableName();
        if (variableName == null) {
            return new ArrayList<>();
        }

        var nameRange = variableName.getTextRange();
        if (nameRange.getStartOffset() == nameRange.getEndOffset()) {
            return new ArrayList<>();
        }

        var range = nameRange.shiftRight(delta);
        var reference = new TextVariableNamePsiReference(psiElement, variable, range, messageBody);

        List<PsiReference> references = new ArrayList<>();
        references.add(reference);

        var variableArgs = variable.getVariableArgs();
        if (variableArgs != null) {
            var argReferences = variableArgs.getVariableArgList().stream()
                .map(arg -> {
                    var argRange = arg.getTextRange().shiftRight(delta);
                    return new TextVariableArgNamePsiReference(psiElement, arg, argRange, messageBody);
                })
                .toArray(PsiReference[]::new);

            references.addAll(Arrays.asList(argReferences));
        }

        return references;
    }

}
