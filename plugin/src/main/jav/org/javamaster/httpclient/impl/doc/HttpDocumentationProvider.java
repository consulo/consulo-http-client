package org.javamaster.httpclient.impl.doc;

import consulo.json.JsonElementTypes;
import consulo.json.psi.JsonLiteral;
import consulo.json.psi.JsonProperty;
import consulo.json.psi.JsonStringLiteral;
import consulo.language.editor.documentation.DocumentationProvider;
import consulo.language.inject.InjectedLanguageManager;
import consulo.navigation.ItemPresentation;
import consulo.codeEditor.Editor;
import consulo.project.Project;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.impl.psi.FakePsiElement;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.impl.js.JsHelper;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.impl.reference.support.HttpVariableNamePsiReference.JsGlobalVariableValueFakePsiElement;
import org.javamaster.httpclient.impl.reference.support.QueryNamePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableNamePsiReference;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
public class HttpDocumentationProvider implements DocumentationProvider {

    @Override
    public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (originalElement == null) {
            return null;
        }

        PsiFile psiFile = InjectedLanguageManager.getInstance(originalElement.getProject()).getTopLevelFile(originalElement);
        if (!(psiFile instanceof HttpFile)) {
            return null;
        }

        if (element instanceof MyFakePsiElement) {
            MyFakePsiElement myFake = (MyFakePsiElement) element;
            HttpVariable variable = myFake.variable;
            HttpVariableName variableName = variable.getVariableName();
            if (variableName == null) {
                return null;
            }

            String name = variableName.getName();

            return getHttpDoc(name, element.getProject(), (HttpFile) psiFile);
        }

        if (element instanceof HttpVariableName) {
            String name = ((HttpVariableName) element).getName();

            return getHttpDoc(name, element.getProject(), (HttpFile) psiFile);
        }

        if (element instanceof HttpGlobalVariableName) {
            HttpGlobalVariableName globalVarName = (HttpGlobalVariableName) element;
            String name = globalVarName.getName();
            HttpGlobalVariable parent = (HttpGlobalVariable) globalVarName.getParent();
            HttpGlobalVariableValue globalVariableValue = parent.getGlobalVariableValue();

            return getDocumentation(
                name,
                NlsBundle.message("value") + " " + (globalVariableValue != null ? globalVariableValue.getText() : "")
            );
        }

        if (element instanceof HttpDirectionName) {
            String name = element.getText();
            ParamEnum paramEnum = ParamEnum.getEnum(name);
            if (paramEnum == null) {
                return null;
            }

            return getDocumentation(name, paramEnum.getDesc());
        }

        if (element instanceof JsonProperty) {
            JsonProperty jsonProperty = (JsonProperty) element;
            boolean match = originalElement.getParent() instanceof HttpVariableReference ||
                    (jsonProperty.getValue() instanceof JsonLiteral &&
                    HttpPsiUtils.getPrevSiblingByType(originalElement.getParent(), JsonElementTypes.COLON, false) != null);
            if (!match) {
                return null;
            }

            String name = jsonProperty.getName();
            String value = EnvFileService.getJsonLiteralValue((JsonLiteral) jsonProperty.getValue());

            return getDocumentation(name, NlsBundle.message("value") + " " + value);
        }

        if (element instanceof PsiDirectory) {
            PsiDirectory directory = (PsiDirectory) element;
            String path = directory.getVirtualFile().getPath();
            PsiElement parent = originalElement.getParent();
            PsiElement psiElement = parent != null ? parent.getParent() : null;

            if (psiElement instanceof HttpVariableName) {
                HttpVariableName varName = (HttpVariableName) psiElement;
                String name = varName.getName();
                InnerVariableEnum variableEnum = InnerVariableEnum.getEnum(name);
                if (variableEnum == null) {
                    return null;
                }

                return getDocumentation(
                    name,
                    variableEnum.typeText() + ", " + NlsBundle.message("value") + " " + path
                );
            }

            if (parent instanceof JsonStringLiteral) {
                return NlsBundle.message("value") + " " + path;
            }

            return null;
        }

        if (element instanceof JsGlobalVariableValueFakePsiElement) {
            JsGlobalVariableValueFakePsiElement jsGlobal = (JsGlobalVariableValueFakePsiElement) element;
            return getDocumentation(jsGlobal.getVariableName(), NlsBundle.message("value") + " " + jsGlobal.getValue());
        }

        return null;
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            Editor editor,
            PsiFile file,
            @Nullable PsiElement contextElement,
            int targetOffset
    ) {
        if (contextElement == null) {
            return null;
        }

        PsiFile psiFile = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(file);
        if (!(psiFile instanceof HttpFile)) {
            return null;
        }

        PsiElement parent = contextElement.getParent();
        if (parent instanceof HttpDirectionName) {
            return parent;
        }

        PsiElement element = parent != null ? parent.getParent() : null;
        if (element instanceof HttpVariableName) {
            return element;
        }

        List<PsiReference> psiReferences = new ArrayList<>();
        for (PsiReference ref : contextElement.getReferences()) {
            psiReferences.add(ref);
        }
        if (parent != null) {
            for (PsiReference ref : parent.getReferences()) {
                psiReferences.add(ref);
            }
        }

        for (PsiReference psiReference : psiReferences) {
            if (psiReference instanceof TextVariableNamePsiReference) {
                TextVariableNamePsiReference textRef = (TextVariableNamePsiReference) psiReference;
                com.intellij.openapi.util.TextRange textRange = textRef.getRangeInElement();
                if (targetOffset < textRange.getStartOffset() || targetOffset > textRange.getEndOffset()) {
                    continue;
                }

                return new MyFakePsiElement(contextElement, textRef.getVariable());
            } else if (psiReference instanceof QueryNamePsiReference) {
                QueryNamePsiReference queryRef = (QueryNamePsiReference) psiReference;
                com.intellij.openapi.util.TextRange textRange = queryRef.getRangeInElement();
                if (targetOffset < textRange.getStartOffset() || targetOffset > textRange.getEndOffset()) {
                    continue;
                }

                return queryRef.resolve();
            }
        }

        return contextElement;
    }

    private @Nullable String getHttpDoc(String name, Project project, HttpFile httpFile) {
        InnerVariableEnum variableEnum = InnerVariableEnum.getEnum(name);
        if (variableEnum != null) {
            return getDocumentation(name, variableEnum.typeText());
        }

        if (name.startsWith(VariableResolver.ENV_PREFIX)) {
            String key = name.substring(VariableResolver.ENV_PREFIX.length() + 1);
            return getDocumentation(
                name,
                "Means System.getenv(\"" + key + "\"), " + NlsBundle.message("value") + " " + System.getenv(key)
            );
        }

        if (name.startsWith(VariableResolver.PROPERTY_PREFIX)) {
            String key = name.substring(VariableResolver.PROPERTY_PREFIX.length() + 1);
            return getDocumentation(
                name,
                "Means System.getProperty(\"" + key + "\"), " + NlsBundle.message("value") + " " + System.getProperty(key)
            );
        }

        String value = JsHelper.getJsGlobalVariable(name);
        if (value != null) {
            return getDocumentation(name, NlsBundle.message("value") + " " + value);
        }

        String selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

        VariableResolver variableResolver = new VariableResolver(null, httpFile, selectedEnv, project);

        String str = "{{" + name + "}}";
        value = variableResolver.resolve(str);
        if (value.equals(str)) {
            return null;
        }

        return getDocumentation(name, NlsBundle.message("value") + " " + value);
    }

    private String getDocumentation(String identifier, String description) {
        return "<div class='definition'><pre>" + identifier + "</pre></div>" + description;
    }

    private static class MyFakePsiElement extends FakePsiElement {
        private final PsiElement contextElement;
        private final HttpVariable variable;

        public MyFakePsiElement(PsiElement contextElement, HttpVariable variable) {
            this.contextElement = contextElement;
            this.variable = variable;
        }

        @Override
        public PsiElement getParent() {
            return contextElement;
        }

        @Override
        public ItemPresentation getPresentation() {
            return MyItemPresentation.INSTANCE;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "(" + variable.getText() + ")";
        }

        private static class MyItemPresentation implements ItemPresentation {
            static final MyItemPresentation INSTANCE = new MyItemPresentation();

            @Override
            public @Nullable String getPresentableText() {
                return "";
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return null;
            }
        }
    }
}
