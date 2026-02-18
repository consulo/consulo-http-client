package consulo.restClient.impl.java;

import com.intellij.java.language.impl.psi.impl.source.PsiClassReferenceType;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;

/**
 * @author yudong
 */
public class PsiUtils {
    public static PsiClass resolvePsiType(PsiType psiType) {
        if (!(psiType instanceof PsiClassReferenceType)) {
            return null;
        }

        return ((PsiClassReferenceType) psiType).resolve();
    }
}
