package consulo.httpClient.impl.java.reference;

import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import org.javamaster.httpclient.psi.HttpQueryParameterKey;
import org.javamaster.httpclient.psi.HttpRequest;
import org.jetbrains.annotations.NotNull;

public class HttpQueryParameterKeyPsiReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        var textRange = element.getTextRange();
        var queryParameterKey = (HttpQueryParameterKey) element;
        var queryName = queryParameterKey.getText();

        var request = PsiTreeUtil.getParentOfType(queryParameterKey, HttpRequest.class);
        if (request == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var requestTarget = request.getRequestTarget();
        if (requestTarget == null) {
            return PsiReference.EMPTY_ARRAY;
        }

        var references = requestTarget.getReferences();
        if (references.length == 0) {
            return PsiReference.EMPTY_ARRAY;
        }

        var resolved = references[0].resolve();
        if (!(resolved instanceof PsiMethod)) {
            return PsiReference.EMPTY_ARRAY;
        }

        var controllerMethod = (PsiMethod) resolved;

        var range = textRange.shiftLeft(textRange.getStartOffset());

        return new PsiReference[]{new QueryNamePsiReference(queryParameterKey, range, controllerMethod, queryName)};
    }

}
