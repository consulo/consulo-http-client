package org.javamaster.httpclient.impl.doc;

import consulo.language.editor.TargetElementUtil;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.editor.documentation.DocumentationProvider;
import consulo.java.language.impl.JavaDocumentationProvider;
import consulo.codeEditor.Editor;
import consulo.java.language.psi.PsiAnnotation;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiMethod;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.Nullable;

/**
 * show SpringMVC Controller method information when hover in url
 *
 * @author yudong
 */
public class HttpUrlControllerMethodDocumentationProvider implements DocumentationProvider {

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            Editor editor,
            PsiFile file,
            @Nullable PsiElement contextElement,
            int targetOffset
    ) {
        TargetElementUtil util = TargetElementUtil.getInstance();
        PsiElement element = util.findTargetElement(editor, util.getAllAccepted(), targetOffset);

        if (element instanceof PsiMethod) {
            return new MyPsiMethod((PsiMethod) element);
        }

        return element;
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!(element instanceof MyPsiMethod)) {
            return null;
        }

        MyPsiMethod myPsiMethod = (MyPsiMethod) element;
        PsiMethod psiMethod = myPsiMethod.psiMethod;

        String str = JavaDocumentationProvider.generateExternalJavadoc(psiMethod, null);

        PsiAnnotation annotation = psiMethod.getAnnotation(HttpUtils.API_OPERATION_ANNO_NAME);
        if (annotation != null) {
            String generateAnno = HttpUtils.generateAnno(annotation);
            return str + generateAnno;
        } else {
            return str;
        }
    }

    private static class MyPsiMethod extends ASTWrapperPsiElement {
        private final PsiMethod psiMethod;

        public MyPsiMethod(PsiMethod psiMethod) {
            super(psiMethod.getNode());
            this.psiMethod = psiMethod;
        }
    }
}
