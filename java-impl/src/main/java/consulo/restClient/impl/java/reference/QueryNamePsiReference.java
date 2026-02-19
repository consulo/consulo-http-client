package consulo.httpClient.impl.java.reference;

import com.intellij.java.language.psi.PsiMethod;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.httpClient.impl.java.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class QueryNamePsiReference extends PsiReferenceBase<PsiElement> {

    private final TextRange textRange;
    private final PsiMethod controllerMethod;
    private final String queryName;

    public QueryNamePsiReference(
        @NotNull PsiElement psiElement,
        TextRange textRange,
        @Nullable PsiMethod controllerMethod,
        String queryName
    ) {
        super(psiElement, textRange);
        this.textRange = textRange;
        this.controllerMethod = controllerMethod;
        this.queryName = queryName;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (controllerMethod == null) {
            return null;
        }

        for (var parameter : controllerMethod.getParameterList().getParameters()) {
            if (queryName.equals(parameter.getName())) {
                return parameter;
            }

            var paramPsiType = parameter.getType();
            var paramPsiCls = PsiUtils.resolvePsiType(paramPsiType);
            if (paramPsiCls == null) {
                continue;
            }

            for (var field : paramPsiCls.getAllFields()) {
                if (queryName.equals(field.getName())) {
                    return field;
                }
            }
        }

        return null;
    }

}
