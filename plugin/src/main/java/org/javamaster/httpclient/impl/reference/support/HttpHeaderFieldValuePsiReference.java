package org.javamaster.httpclient.impl.reference.support;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiClass;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiMethod;
import consulo.language.psi.PsiReferenceBase;
import consulo.java.language.impl.psi.search.ClassInheritorsSearch;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.psi.HttpHeaderField;
import org.javamaster.httpclient.psi.HttpHeaderFieldValue;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.impl.utils.DubboUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpHeaderFieldValuePsiReference extends PsiReferenceBase<HttpHeaderFieldValue> {

    public HttpHeaderFieldValuePsiReference(@NotNull HttpHeaderFieldValue fieldValue, TextRange range) {
        super(fieldValue, range);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var headerField = (HttpHeaderField) getElement().getParent();
        var fieldName = headerField.getName();

        if (DubboUtils.INTERFACE_KEY.equals(fieldName)) {
            return resolveInterface(getElement());
        }

        if (DubboUtils.METHOD_KEY.equals(fieldName)) {
            return resolveMethod(headerField, getElement().getText());
        }

        return null;
    }

    @Nullable
    private PsiClass resolveInterface(HttpHeaderFieldValue fieldValue) {
        var module = DubboUtils.getOriginalModule(fieldValue);
        if (module == null) {
            return null;
        }

        return DubboUtils.findInterface(module, fieldValue.getText());
    }

    @Nullable
    private PsiMethod resolveMethod(HttpHeaderField headerField, String methodName) {
        var httpRequest = PsiTreeUtil.getParentOfType(headerField, HttpRequest.class);
        if (httpRequest == null) {
            return null;
        }

        var header = httpRequest.getHeader();
        if (header == null) {
            return null;
        }

        var interfaceField = header.getInterfaceField();
        if (interfaceField == null) {
            return null;
        }

        var interfaceFieldValue = interfaceField.getHeaderFieldValue();
        if (interfaceFieldValue == null) {
            return null;
        }

        var psiClass = resolveInterface(interfaceFieldValue);
        if (psiClass == null) {
            return null;
        }

        for (var inheritor : ClassInheritorsSearch.search(psiClass).findAll()) {
            var methods = inheritor.findMethodsByName(methodName, false);
            if (methods.length == 0) {
                return null;
            }

            if (methods.length > 1) {
                System.out.println("Founded " + methods.length + " methods of the same name, get the first one");
            }

            return methods[0];
        }

        return null;
    }
}
