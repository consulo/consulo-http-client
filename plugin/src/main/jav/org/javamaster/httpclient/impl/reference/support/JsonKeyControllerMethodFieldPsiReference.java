package org.javamaster.httpclient.impl.reference.support;

import consulo.json.psi.JsonStringLiteral;
import consulo.document.util.TextRange;
import consulo.language.psi.*;
import consulo.java.language.impl.psi.source.PsiClassReferenceType;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class JsonKeyControllerMethodFieldPsiReference extends PsiReferenceBase<JsonStringLiteral> {

    private final JsonStringLiteral jsonString;
    private final PsiMethod controllerMethod;

    public JsonKeyControllerMethodFieldPsiReference(
        @NotNull JsonStringLiteral jsonString,
        @NotNull PsiMethod controllerMethod,
        TextRange range
    ) {
        super(jsonString, range);
        this.jsonString = jsonString;
        this.controllerMethod = controllerMethod;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var virtualFile = jsonString.getContainingFile().getVirtualFile();

        PsiType paramPsiType;
        if (virtualFile != null && virtualFile.getName().endsWith("res.http")) {
            paramPsiType = controllerMethod.getReturnType();
        } else {
            var psiParameter = HttpUtils.resolveTargetParam(controllerMethod);

            paramPsiType = psiParameter != null ? psiParameter.getType() : null;
        }

        var paramPsiCls = PsiUtils.resolvePsiType(paramPsiType);
        if (paramPsiCls == null) {
            return null;
        }

        var classGenericParameters = ((PsiClassReferenceType) paramPsiType).getParameters();

        var jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString);

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters);
    }

}
