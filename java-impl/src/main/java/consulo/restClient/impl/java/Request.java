package consulo.restClient.impl.java;

import com.intellij.java.language.psi.PsiMethod;

import java.util.Objects;

public class Request {
    private final org.javamaster.httpclient.model.HttpMethod method;
    private final String path;
    private final PsiMethod psiElement;

    public Request(org.javamaster.httpclient.model.HttpMethod tmpMethod, String tmpPath, PsiMethod psiElement, Request parent) {
        this.psiElement = psiElement;

        if (parent == null) {
            String path = tmpPath.trim();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            path = path.replace("//", "/");

            this.method = tmpMethod;
            this.path = path;
        } else {
            if (tmpMethod == org.javamaster.httpclient.model.HttpMethod.REQUEST) {
                this.method = parent.method;
            } else {
                this.method = tmpMethod;
            }

            String parentPath = parent.path;
            if (parentPath.endsWith("/")) {
                parentPath = parentPath.substring(0, parentPath.length() - 1);
            }

            this.path = parentPath + tmpPath;
        }
    }

    public Request copyWithParent(Request parent) {
        return new Request(this.method, this.path, this.psiElement, parent);
    }

    @Override
    public String toString() {
        return this.path + "-" + method.name();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Request request = (Request) other;

        if (!Objects.equals(psiElement, request.psiElement)) return false;
        if (method != request.method) return false;
        return Objects.equals(path, request.path);
    }

    @Override
    public int hashCode() {
        int result = psiElement != null ? psiElement.hashCode() : 0;
        result = 31 * result + method.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    public org.javamaster.httpclient.model.HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public PsiMethod getPsiElement() {
        return psiElement;
    }
}
