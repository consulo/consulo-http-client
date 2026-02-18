package consulo.restClient.impl.java.spring;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiNameValuePair;
import consulo.language.editor.inlay.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import org.javamaster.httpclient.NlsBundle;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 */
public class HttpInlayHintsCollector implements DeclarativeInlayHintsCollector.SharedBypassCollector {
    private static final Set<String> MVC_ANNO_SET = new HashSet<>();

    static {
        MVC_ANNO_SET.add(SpringHttpMethod.REQUEST_MAPPING.getQualifiedName());
        MVC_ANNO_SET.add(SpringHttpMethod.GET_MAPPING.getQualifiedName());
        MVC_ANNO_SET.add(SpringHttpMethod.POST_MAPPING.getQualifiedName());
        MVC_ANNO_SET.add(SpringHttpMethod.PATCH_MAPPING.getQualifiedName());
        MVC_ANNO_SET.add(SpringHttpMethod.PUT_MAPPING.getQualifiedName());
        MVC_ANNO_SET.add(SpringHttpMethod.DELETE_MAPPING.getQualifiedName());
    }

    @Override
    public void collectFromElement(@NotNull PsiElement element, @NotNull DeclarativeInlayTreeSink sink) {
        if (!(element instanceof PsiLiteralExpression)) {
            return;
        }

        PsiElement nameValuePair = element.getParent();
        if (!(nameValuePair instanceof PsiNameValuePair)) {
            return;
        }

        PsiElement psiAnnoParent = nameValuePair.getParent();
        if (psiAnnoParent == null) {
            return;
        }

        PsiElement psiAnno = psiAnnoParent.getParent();
        if (!(psiAnno instanceof PsiAnnotation)) {
            return;
        }

        String qualifiedName = ((PsiAnnotation) psiAnno).getQualifiedName();
        if (!MVC_ANNO_SET.contains(qualifiedName)) {
            return;
        }

        SmartPsiElementPointer<PsiElement> pointer = SmartPointerManager.getInstance(element.getProject())
                .createSmartPsiElementPointer(element);

        sink.addPresentation(
                new DeclarativeInlayPosition.InlineInlayPosition(element.getTextRange().getStartOffset(), false),
                null,
                NlsBundle.message("create.http.req"),
                false,
                builder -> {
                    builder.text(
                            "url",
                            new InlayActionData(new InlayActionPayload.PsiPointerInlayActionPayload(pointer), "HttpInlayHintsCollector")
                    );
                }
        );
    }
}
