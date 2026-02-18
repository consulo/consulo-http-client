package org.javamaster.httpclient.impl.jsPlugin.support;

import consulo.container.plugin.PluginDescriptor;
import consulo.container.plugin.PluginId;
import consulo.container.plugin.PluginManager;
import consulo.document.util.TextRange;
import consulo.fileEditor.FileEditorManager;
import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.util.lang.Pair;
import org.javamaster.httpclient.impl.jsPlugin.JsFacade;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class JavaScript {
    private static ClassLoader pluginClassLoader;
    private static Class<? extends PsiElement> clzJSCallExpression;
    private static Class<? extends PsiElement> clzJSReferenceExpression;
    private static Class<? extends PsiElement> clzJSArgumentList;
    private static Class<? extends PsiElement> clzJSLiteralExpression;

    static {
        IdeaPluginDescriptor plugin = findPlugin();
        if (plugin != null) {
            pluginClassLoader = plugin.getPluginClassLoader();

            if (pluginClassLoader != null) {
                try {
                    clzJSCallExpression = loadClass("com.intellij.lang.javascript.psi.JSCallExpression");
                    clzJSReferenceExpression = loadClass("com.intellij.lang.javascript.psi.JSReferenceExpression");
                    clzJSArgumentList = loadClass("com.intellij.lang.javascript.psi.JSArgumentList");
                    clzJSLiteralExpression = loadClass("com.intellij.lang.javascript.psi.JSLiteralExpression");


                } catch (Exception e) {
                    pluginClassLoader = null;
                }
            }
        }
    }

    private JavaScript() {
    }

    @Nullable
    public static PsiElement resolveJsVariable(
            String variableName,
            Project project,
            List<HttpScriptBody> scriptBodyList
    ) {
        if (pluginClassLoader == null) {
            return null;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
        for (HttpScriptBody scriptBody : scriptBodyList) {
            List<Pair<PsiElement, TextRange>> injectedPsiFiles = injectedLanguageManager.getInjectedPsiFiles(scriptBody);
            if (injectedPsiFiles == null || injectedPsiFiles.isEmpty()) {
                continue;
            }

            PsiElement jsFile = injectedPsiFiles.get(0).getFirst();

            PsiElement result = resolveJsVariable(variableName, jsFile);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Nullable
    public static PsiElement resolveJsVariable(String variableName, PsiFile jsFile) {
        if (pluginClassLoader == null) {
            return null;
        }

        return resolveJsVariable(variableName, (PsiElement) jsFile);
    }

    @Nullable
    private static PsiElement resolveJsVariable(String variableName, PsiElement jsFile) {
        for (PsiElement expression : PsiTreeUtil.findChildrenOfType(jsFile, clzJSCallExpression)) {
            PsiElement dotExpression = PsiTreeUtil.getChildOfType(expression, clzJSReferenceExpression);
            if (dotExpression == null) {
                continue;
            }

            PsiElement arguments = PsiTreeUtil.getChildOfType(expression, clzJSArgumentList);
            if (arguments == null) {
                continue;
            }

            if (JsFacade.INTERESTED_EXPRESSIONS.contains(dotExpression.getText())) {
                PsiElement result = findArgumentName(variableName, arguments, clzJSLiteralExpression);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    @Nullable
    private static PsiElement findArgumentName(String variableName, PsiElement arguments, Class<? extends PsiElement> clz) {
        PsiElement jsLiteralExpression = PsiTreeUtil.getChildOfType(arguments, clz);
        if (jsLiteralExpression == null) {
            return null;
        }

        String text = jsLiteralExpression.getText();
        if (text.length() >= 2 && text.substring(1, text.length() - 1).equals(variableName)) {
            return jsLiteralExpression;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends PsiElement> loadClass(String clzName) {
        try {
            return (Class<? extends PsiElement>) pluginClassLoader.loadClass(clzName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static PluginDescriptor findPlugin() {
        PluginId pluginId = PluginId.findId("JavaScript");
        if (pluginId == null) {
            return null;
        }
        return PluginManager.getInstance().findEnabledPlugin(pluginId);
    }

    @Nullable
    public static PsiElement createJsVariable(Project project, PsiFile injectedPsiFile, String variableName) {
        if (pluginClassLoader == null) {
            return null;
        }

        try {
            Class<? extends PsiElement> clzJSExpressionStatement = loadClass("com.intellij.lang.javascript.psi.JSExpressionStatement");

            String js = "request.variables.set('" + variableName + "', '');\n";

            PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);

            PsiFile tmpFile = psiFileFactory.createFileFromText("dummy.js", jsLanguage, js);
            PsiElement newExpressionStatement = PsiTreeUtil.findChildOfType(tmpFile, clzJSExpressionStatement);
            if (newExpressionStatement == null) {
                return null;
            }

            PsiElement elementCopy = injectedPsiFile.add(newExpressionStatement);
            injectedPsiFile.add(newExpressionStatement.getNextSibling());

            // Move the cursor into the quotation marks
            ((Navigatable) elementCopy.getLastChild()).navigate(true);
            var caretModel = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (caretModel != null) {
                caretModel.getCaretModel().moveToOffset(caretModel.getCaretModel().getOffset() - 2);
            }

            return newExpressionStatement;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAvailable() {
        return pluginClassLoader != null;
    }
}
