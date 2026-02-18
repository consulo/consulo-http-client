package org.javamaster.httpclient.impl.reference.support;

import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.module.ModuleUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.psi.HttpPathAbsolute;
import org.javamaster.httpclient.impl.scan.ScanRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpPathAbsolutePsiReference extends PsiReferenceBase<HttpPathAbsolute> {

    private final HttpPathAbsolute httpPathAbsolute;

    public HttpPathAbsolutePsiReference(
        @NotNull HttpPathAbsolute httpPathAbsolute,
        TextRange textRange
    ) {
        super(httpPathAbsolute, textRange);
        this.httpPathAbsolute = httpPathAbsolute;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        var module = ModuleUtil.findModuleForPsiElement(httpPathAbsolute);
        if (module == null) {
            return new Object[0];
        }

        var map = ScanRequest.getCacheRequestMap(module, module.getProject());

        return map.values().stream()
            .flatMap(list -> list.stream()
                .map(innerIt -> {
                    var psiElement = innerIt.getPsiElement();
                    if (psiElement == null) {
                        return null;
                    }

                    var containingClass = psiElement.getContainingClass();
                    if (containingClass == null) {
                        return null;
                    }

                    var name = containingClass.getName();
                    if (name == null) {
                        return null;
                    }

                    return LookupElementBuilder
                        .create(innerIt.getPath())
                        .appendTailText("[" + innerIt.getMethod().name() + "]", true)
                        .withIcon(HttpIcons.REQUEST_MAPPING)
                        .withTypeText(name);
                })
            )
            .filter(it -> it != null)
            .toArray();
    }

}
