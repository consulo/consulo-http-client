package consulo.restClient.impl.java.dubbo;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.json.psi.JsonStringLiteral;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.psi.HttpHeaderField;
import org.javamaster.httpclient.psi.HttpHeaderFieldValue;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.utils.DubboUtilsPart;
import org.javamaster.httpclient.utils.HttpUtilsPart;

public class DubboUtils extends DubboUtilsPart {
    public static PsiClass findInterface(Module module, String name) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(module.getProject());
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);

        return javaPsiFacade.findClass(name, scope);
    }

    public static PsiClass findInterface(Project project, String name) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        return javaPsiFacade.findClass(name, scope);
    }

    public static PsiMethod findDubboServiceMethod(JsonStringLiteral jsonString) {
        HttpMessageBody httpMessageBody =
            (HttpMessageBody) InjectedLanguageManager.getInstance(jsonString.getProject()).getInjectionHost(jsonString);
        HttpRequest httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest.class);

        if (httpRequest == null || httpRequest.getHeader() == null) {
            return null;
        }

        HttpHeaderField headerField = httpRequest.getHeader().getHeaderFieldList()
            .stream()
            .filter(it -> it.getHeaderFieldName().getText().equalsIgnoreCase(INTERFACE_KEY))
            .findFirst()
            .orElse(null);

        if (headerField == null) {
            return null;
        }

        Module module = getOriginalModule(httpRequest);
        if (module == null) {
            return null;
        }

        String name = headerField.getHeaderFieldValue() != null ? headerField.getHeaderFieldValue().getText() : null;
        if (name == null) {
            return null;
        }

        PsiClass psiClass = findInterface(module, name);
        if (psiClass == null) {
            return null;
        }

        HttpHeaderField headerFieldMethod = httpRequest.getHeader().getHeaderFieldList()
            .stream()
            .filter(it -> it.getHeaderFieldName().getText().equalsIgnoreCase(METHOD_KEY))
            .findFirst()
            .orElse(null);

        if (headerFieldMethod == null) {
            return null;
        }

        String value = headerFieldMethod.getHeaderFieldValue() != null ? headerFieldMethod.getHeaderFieldValue().getText() : null;
        if (value == null) {
            return null;
        }

        PsiMethod[] methods = psiClass.findMethodsByName(value, false);
        if (methods.length == 0) {
            return null;
        }

        return methods[0];
    }

    private static VirtualFile getOriginalFile(HttpHeaderFieldValue headerFieldValue) {
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(headerFieldValue);
        if (!HttpUtilsPart.isFileInIdeaDir(virtualFile)) {
            return virtualFile;
        }

        HttpRequest httpRequest = PsiTreeUtil.getParentOfType(headerFieldValue, HttpRequest.class);
        if (httpRequest == null) {
            return null;
        }

        return getOriginalFile(httpRequest);
    }

    private static VirtualFile getOriginalFile(HttpRequest httpRequest) {
        VirtualFile virtualFile = PsiUtil.getVirtualFile(httpRequest);
        if (!HttpUtilsPart.isFileInIdeaDir(virtualFile)) {
            return virtualFile;
        }

        String tabName = HttpUtilsPart.getTabName(httpRequest.getMethod());

        return HttpUtilsPart.getOriginalFile(httpRequest.getProject(), tabName);
    }

    private static Module getOriginalModule(HttpRequest httpRequest) {
        Project project = httpRequest.getProject();

        VirtualFile virtualFile = getOriginalFile(httpRequest);
        if (virtualFile == null) {
            return null;
        }

        return ModuleUtilCore.findModuleForFile(virtualFile, project);
    }

    public static Module getOriginalModule(HttpHeaderFieldValue headerFieldValue) {
        Project project = headerFieldValue.getProject();

        VirtualFile virtualFile = getOriginalFile(headerFieldValue);
        if (virtualFile == null) {
            return null;
        }

        return ModuleUtilCore.findModuleForFile(virtualFile, project);
    }
}
