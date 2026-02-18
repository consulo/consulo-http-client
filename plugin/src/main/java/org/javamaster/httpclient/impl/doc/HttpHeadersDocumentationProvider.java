package org.javamaster.httpclient.impl.doc;

import consulo.language.editor.documentation.DocumentationProvider;
import consulo.codeEditor.Editor;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.ast.TokenSet;
import consulo.util.collection.SmartList;
import org.javamaster.httpclient.factory.HttpPsiFactory;
import org.javamaster.httpclient.impl.completion.support.HttpHeadersDictionary;
import org.javamaster.httpclient.impl.doc.support.HttpHeaderDocumentation;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author yudong
 */
public class HttpHeadersDocumentationProvider implements DocumentationProvider {
    private final TokenSet headerSet = TokenSet.create(HttpTypes.FIELD_NAME, HttpTypes.FIELD_VALUE);

    @Override
    public @Nullable List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        HttpHeaderDocumentation doc = getDocumentation(element);
        if (doc == null) {
            return null;
        }

        return new SmartList<>(doc.getUrl());
    }

    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        HttpHeaderDocumentation doc = getDocumentation(element);
        if (doc == null) {
            return null;
        }

        return doc.generateDoc();
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLookupItem(
            PsiManager psiManager,
            Object object,
            PsiElement element
    ) {
        if (!(object instanceof HttpHeaderDocumentation)) {
            return null;
        }

        HttpHeaderDocumentation headerDoc = (HttpHeaderDocumentation) object;
        String name = headerDoc.getName();
        if (psiManager == null) {
            return null;
        }

        if (StringUtil.isEmpty(name)) {
            return element;
        }

        HttpFile file = HttpPsiFactory.createDummyFile(psiManager.getProject(), "GET http://127.0.0.1\n" + name + " : ");
        HttpRequest newRequest = file.getRequestBlocks().get(0).getRequest();

        return newRequest.getHeader().getHeaderFieldList().get(0);
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            Editor editor,
            PsiFile file,
            @Nullable PsiElement contextElement,
            int targetOffset
    ) {
        PsiElement psiElement = contextElement;
        if (psiElement == null) {
            return null;
        }

        if (!(file instanceof HttpFile)) {
            return null;
        }

        while (psiElement instanceof PsiWhiteSpace || HttpPsiUtils.isOfType(psiElement, HttpTypes.COLON)) {
            psiElement = psiElement.getPrevSibling();
            if (psiElement == null) {
                return null;
            }
        }

        if (HttpPsiUtils.isOfTypes(psiElement, headerSet)) {
            psiElement = psiElement.getParent();
        }

        if (psiElement instanceof HttpHeaderFieldName || psiElement instanceof HttpHeaderFieldValue) {
            return psiElement.getParent();
        }

        return null;
    }

    private @Nullable HttpHeaderDocumentation getDocumentation(@Nullable PsiElement element) {
        if (!(element instanceof HttpHeaderField)) {
            return null;
        }

        HttpHeaderField headerField = (HttpHeaderField) element;
        String name = headerField.getName();

        return HttpHeadersDictionary.getDocumentation(name);
    }
}
