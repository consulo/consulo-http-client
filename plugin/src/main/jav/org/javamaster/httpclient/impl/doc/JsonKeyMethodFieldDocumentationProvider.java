package org.javamaster.httpclient.impl.doc;

import consulo.language.editor.TargetElementUtil;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.editor.documentation.DocumentationProvider;
import consulo.java.language.impl.JavaDocumentationProvider;
import consulo.codeEditor.Editor;
import consulo.java.language.psi.PsiAnnotation;
import consulo.language.psi.PsiElement;
import consulo.java.language.psi.PsiField;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.Nullable;

/**
 * show SpringMVC Controller or Dubbo Service method param field information when hover in json key
 *
 * @author yudong
 */
public class JsonKeyMethodFieldDocumentationProvider implements DocumentationProvider {

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            Editor editor,
            PsiFile file,
            @Nullable PsiElement contextElement,
            int targetOffset
    ) {
        TargetElementUtil util = TargetElementUtil.getInstance();
        PsiElement element = util.findTargetElement(editor, util.getAllAccepted(), targetOffset);

        if (element instanceof PsiField && ((PsiField) element).getNode() != null) {
            return new MyPsiField((PsiField) element);
        }

        return element;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!(element instanceof MyPsiField)) {
            return null;
        }

        MyPsiField myPsiField = (MyPsiField) element;
        PsiField psiField = myPsiField.psiField;

        String str = JavaDocumentationProvider.generateExternalJavadoc(psiField, null);

        PsiAnnotation annotation = psiField.getAnnotation(HttpUtils.API_MODEL_PROPERTY_ANNO_NAME);
        if (annotation != null) {
            String generateAnno = HttpUtils.generateAnno(annotation);
            return str + generateAnno;
        } else {
            return str;
        }
    }

    private static class MyPsiField extends ASTWrapperPsiElement {
        private final PsiField psiField;

        public MyPsiField(PsiField psiField) {
            super(psiField.getNode());
            this.psiField = psiField;
        }
    }
}
