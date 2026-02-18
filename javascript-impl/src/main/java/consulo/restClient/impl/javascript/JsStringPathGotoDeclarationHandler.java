package consulo.restClient.impl.javascript;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.navigation.GotoDeclarationHandler;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
@ExtensionImpl(id = "JsStringPathGotoDeclarationHandler")
public class JsStringPathGotoDeclarationHandler implements GotoDeclarationHandler {


    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement element, int offset, Editor editor) {
        if (element == null) {
            return PsiElement.EMPTY_ARRAY;
        }

        var project = element.getProject();

        var injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element);
        if (!(injectionHost instanceof HttpScriptBody)) {
            return PsiElement.EMPTY_ARRAY;
        }

        if (!(element.getParent() instanceof JSLiteralExpression)) {
            return PsiElement.EMPTY_ARRAY;
        }

        var text = element.getText();
        if (text.length() < 3) {
            return PsiElement.EMPTY_ARRAY;
        }

        var path = text.substring(1, text.length() - 1);

        try {
            var httpFileParentPath = injectionHost.getContainingFile().getVirtualFile().getParent().getPath();

            var tmpPath = VariableResolver.resolveInnerVariable(path, httpFileParentPath, project);

            var item = HttpUtilsPart.resolveFilePath(tmpPath, httpFileParentPath, project);
            if (item == null) {
                return PsiElement.EMPTY_ARRAY;
            }

            return new PsiElement[]{item};
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return PsiElement.EMPTY_ARRAY;
    }

}
