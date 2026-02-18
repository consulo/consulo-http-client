package org.javamaster.httpclient.impl.reference.support;

import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.json.psi.JsonProperty;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.document.util.TextRange;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.impl.psi.FakePsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.impl.completion.support.SlashEndInsertHandler;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.impl.js.JsHelper;
import org.javamaster.httpclient.impl.jsPlugin.JsFacade;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpFilePath;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.psi.HttpVariableName;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.javamaster.httpclient.impl.resolve.VariableResolver.ENV_PREFIX;
import static org.javamaster.httpclient.impl.resolve.VariableResolver.PROPERTY_PREFIX;

/**
 * @author yudong
 */
public class HttpVariableNamePsiReference extends PsiReferenceBase<HttpVariableName> {

    private final TextRange textRange;

    public HttpVariableNamePsiReference(@NotNull HttpVariableName element, TextRange textRange) {
        super(element, textRange);
        this.textRange = textRange;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return tryResolveVariable(getElement().getName(), getElement().isBuiltin(), getElement(), true);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return getVariableVariants(getElement());
    }

    public static Object[] getVariableVariants(PsiElement element) {
        List<Object> allList = new ArrayList<>();

        var parent = element.getParent();
        if (parent != null && parent.getParent() instanceof HttpFilePath) {
            var tmp = InnerVariableEnum.MVN_TARGET;
            allList.add(
                LookupElementBuilder.create(tmp.getMethodName()).withTypeText(tmp.typeText(), true)
                    .withInsertHandler(SlashEndInsertHandler.INSTANCE)
            );

            tmp = InnerVariableEnum.PROJECT_ROOT;
            allList.add(
                LookupElementBuilder.create(tmp.getMethodName()).withTypeText(tmp.typeText(), true)
                    .withInsertHandler(SlashEndInsertHandler.INSTANCE)
            );

            tmp = InnerVariableEnum.HISTORY_FOLDER;
            allList.add(
                LookupElementBuilder.create(tmp.getMethodName()).withTypeText(tmp.typeText(), true)
                    .withInsertHandler(SlashEndInsertHandler.INSTANCE)
            );

            return allList.toArray();
        }

        var envVariables = EnvFileService.getEnvMap(element.getProject());
        var list = envVariables.entrySet().stream()
            .map(entry -> LookupElementBuilder.create(entry.getKey()).withTypeText(entry.getValue(), true))
            .collect(Collectors.toList());

        allList.addAll(list);

        allList.addAll(getBuiltInFunList());

        var propertyList = System.getProperties().entrySet().stream()
            .map(entry -> LookupElementBuilder.create(PROPERTY_PREFIX + "." + entry.getKey())
                .withTypeText("" + entry.getValue(), true))
            .collect(Collectors.toList());
        allList.addAll(propertyList);

        var envList = System.getenv().entrySet().stream()
            .map(entry -> LookupElementBuilder.create(ENV_PREFIX + "." + entry.getKey())
                .withTypeText(entry.getValue(), true))
            .collect(Collectors.toList());
        allList.addAll(envList);

        return allList.toArray();
    }

    @Nullable
    public static PsiElement tryResolveVariable(
        String variableName,
        boolean builtin,
        PsiElement element,
        boolean searchInPreJs
    ) {
        var httpFile = element.getContainingFile();
        var project = httpFile.getProject();

        var virtualFile = httpFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        var parent = virtualFile.getParent();
        if (parent == null) {
            return null;
        }

        var httpFileParentPath = parent.getPath();

        if (builtin) {
            var innerVariableEnum = InnerVariableEnum.getEnum(variableName);

            if (InnerVariableEnum.isFolderEnum(innerVariableEnum)) {
                var path = innerVariableEnum.exec(httpFileParentPath, project);
                if (path == null) {
                    return null;
                }

                var vFile = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(path));
                if (vFile == null) {
                    return null;
                }

                return PsiManager.getInstance(project).findDirectory(vFile);
            }

            return null;
        }

        var fileGlobalVariable = HttpUtils.resolveFileGlobalVariable(variableName, httpFile);
        if (fileGlobalVariable != null) {
            return fileGlobalVariable;
        }

        var jsElement = tryResolveInJsHandler(variableName, element, httpFile, project, searchInPreJs);
        if (jsElement != null) {
            return jsElement;
        }

        var selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

        var jsonLiteral = EnvFileService.getEnvEleLiteral(variableName, selectedEnv, httpFileParentPath, project);

        var jsonProperty = PsiTreeUtil.getParentOfType(jsonLiteral, JsonProperty.class);
        if (jsonProperty != null) {
            return jsonProperty;
        }

        var value = JsHelper.getJsGlobalVariable(variableName);
        if (value != null) {
            return new JsGlobalVariableValueFakePsiElement(element, variableName, value);
        }

        return null;
    }

    @Nullable
    private static PsiElement tryResolveInJsHandler(
        String variableName,
        PsiElement element,
        PsiFile httpFile,
        Project project,
        boolean searchInPreJs
    ) {
        var requestBlock = PsiTreeUtil.getParentOfType(element, HttpRequestBlock.class);
        if (requestBlock == null) {
            return null;
        }

        if (searchInPreJs) {
            var scriptBodyList = HttpUtils.getAllPreJsScripts(httpFile, requestBlock);
            var reversed = new ArrayList<>(scriptBodyList);
            java.util.Collections.reverse(reversed);

            var jsVariable = JsFacade.resolveJsVariable(variableName, project, reversed);
            if (jsVariable != null) {
                return jsVariable;
            }

            var preJsFiles = HttpUtils.getPreJsFiles((HttpFile) httpFile, true);

            var resolved = JsFacade.resolveJsVariable(variableName, preJsFiles);
            if (resolved != null) {
                return resolved;
            }
        }

        var scriptBodyList = HttpUtils.getAllPostJsScripts(httpFile);

        return JsFacade.resolveJsVariable(variableName, project, scriptBodyList);
    }

    private static List<Object> getBuiltInFunList() {
        return Arrays.stream(InnerVariableEnum.values())
            .map(it -> LookupElementBuilder.create(it.getMethodName())
                .withInsertHandler(it.insertHandler())
                .withTypeText(it.typeText(), true))
            .collect(Collectors.toList());
    }

    public static class JsGlobalVariableValueFakePsiElement extends FakePsiElement {
        private final PsiElement element;
        private final String variableName;
        private final String value;

        public JsGlobalVariableValueFakePsiElement(PsiElement element, String variableName, String value) {
            this.element = element;
            this.variableName = variableName;
            this.value = value;
        }

        @Override
        public PsiElement getParent() {
            return element;
        }

        @Override
        public boolean canNavigate() {
            return false;
        }

        @Override
        public void navigate(boolean requestFocus) {
        }

        @Override
        public ItemPresentation getPresentation() {
            return MyItemPresentation.INSTANCE;
        }

        private static class MyItemPresentation implements ItemPresentation {
            static final MyItemPresentation INSTANCE = new MyItemPresentation();

            @Override
            public String getPresentableText() {
                return "";
            }

            @jakarta.annotation.Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Image getIcon() {
                return null;
            }
        }
    }
}
