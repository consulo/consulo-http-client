package consulo.restClient.impl.java.reference;

import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.document.util.TextRange;
import consulo.restClient.impl.java.ScanRequest;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequestTarget;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpUrlControllerMethodPsiReference extends PsiReferenceBase<HttpRequestTarget> {

    private final String searchTxt;
    private final HttpRequestTarget requestTarget;

    public HttpUrlControllerMethodPsiReference(
        @NotNull String searchTxt,
        @NotNull HttpRequestTarget requestTarget,
        TextRange textRange
    ) {
        super(requestTarget, textRange);
        this.searchTxt = searchTxt;
        this.requestTarget = requestTarget;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        var containingFile = getElement().getContainingFile();
        if (containingFile == null) {
            return null;
        }

        var virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        var module = findModule(requestTarget, virtualFile);
        if (module == null) {
            return null;
        }

        var httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod.class);
        if (httpMethod == null) {
            return null;
        }

        return ScanRequest.findApiMethod(module, searchTxt, httpMethod.getText());
    }

    @Nullable
    private Module findModule(HttpRequestTarget requestTarget, VirtualFile virtualFile) {
        if (HttpUtilsPart.isFileInIdeaDir(virtualFile)) {
            return HttpUtilsPart.getOriginalModule(requestTarget);
        } else {
            return ModuleUtilCore.findModuleForPsiElement(requestTarget);
        }
    }

}
