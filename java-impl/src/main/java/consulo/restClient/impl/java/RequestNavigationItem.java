package consulo.httpClient.impl.java;

import com.intellij.java.language.psi.PsiMethod;
import consulo.language.impl.psi.FakePsiElement;
import consulo.language.psi.PsiElement;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class RequestNavigationItem extends FakePsiElement {
    private final Request request;
    private final PsiMethod psiMethod;

    public RequestNavigationItem(Request request) {
        this.request = request;
        this.psiMethod = request.getPsiElement();
    }

    public Request getRequest() {
        return request;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new RequestItemPresentation(request);
    }

    @Override
    public Image getIcon(boolean open) {
        return request.getMethod().getIcon();
    }

    @Override
    public boolean canNavigate() {
        return psiMethod.canNavigate();
    }

    @Override
    public void navigate(boolean requestFocus) {
        psiMethod.navigate(requestFocus);
    }

    @Override
    public String getName() {
        return request.getPath();
    }

    @Nullable
    @Override
    public PsiElement getContext() {
        return psiMethod.getContext();
    }

    @NotNull
    @Override
    public PsiElement getParent() {
        return psiMethod;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return psiMethod.getNavigationElement();
    }

    @Override
    public boolean isValid() {
        return psiMethod.isValid();
    }

    public static class RequestItemPresentation implements ItemPresentation {
        private final Request request;

        public RequestItemPresentation(Request request) {
            this.request = request;
        }

        @Override
        public String getPresentableText() {
            PsiMethod psiMethod = request.getPsiElement();

            String str = JavaHttpUtils.getMethodDesc(psiMethod);

            if (!str.isEmpty()) {
                str = "(" + str + ")";
            } else {
                str = "";
            }

            return request.getPath() + str;
        }

        @Nullable
        @Override
        public String getLocationString() {
            return null;
        }

        @Override
        public Image getIcon() {
            return request.getMethod().getIcon();
        }
    }
}
