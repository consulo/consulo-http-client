package consulo.restClient.impl.java.dubbo;

import consulo.json.psi.JsonStringLiteral;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiType;
import consulo.java.language.impl.psi.source.PsiClassReferenceType;
import org.javamaster.httpclient.impl.utils.DubboUtils;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author yudong
 */
public class JsonKeyDubboMethodFieldPsiReference extends PsiReferenceBase<JsonStringLiteral> {

    private final JsonStringLiteral jsonString;

    public JsonKeyDubboMethodFieldPsiReference(
        @NotNull JsonStringLiteral jsonString,
        TextRange range
    ) {
        super(jsonString, range);
        this.jsonString = jsonString;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var virtualFile = jsonString.getContainingFile().getVirtualFile();

        var method = DubboUtils.findDubboServiceMethod(jsonString);
        if (method == null) {
            return null;
        }

        var jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString);

        PsiType paramPsiType;
        if (virtualFile != null && virtualFile.getName().endsWith("res.http")) {
            paramPsiType = method.getReturnType();
        } else {
            var name = jsonPropertyNameLevels.pop();
            var psiParameter = Arrays.stream(method.getParameterList().getParameters())
                .filter(parameter -> name.equals(parameter.getName()))
                .findFirst()
                .orElse(null);

            if (psiParameter == null) {
                return null;
            }

            if (jsonPropertyNameLevels.isEmpty()) {
                return psiParameter;
            }

            paramPsiType = psiParameter.getType();
        }

        var paramPsiCls = PsiUtils.resolvePsiType(paramPsiType);
        if (paramPsiCls == null) {
            return null;
        }

        var classGenericParameters = ((PsiClassReferenceType) paramPsiType).getParameters();

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters);
    }

}
