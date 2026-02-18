package org.javamaster.httpclient.impl.inspection.fix;

import consulo.application.Application;
import consulo.codeEditor.CaretModel;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.json.psi.JsonStringLiteral;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.PriorityAction;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.restClient.localize.RestClientLocalize;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.jsPlugin.JsFacade;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;

import java.util.Collections;
import java.util.List;

/**
 * @author yudong
 */
public class CreateJsVariableQuickFix implements LocalQuickFix, PriorityAction {

    private final boolean global;
    private final String variableName;

    public CreateJsVariableQuickFix(boolean global, String variableName) {
        this.global = global;
        this.variableName = variableName;
    }

    @Override
    public LocalizeValue getName() {
        LocalizeValue tip = global ? RestClientLocalize.global() : RestClientLocalize.preRequest();
        return RestClientLocalize.unsolvedHandlerVariable(tip);
    }

    @Override
    public void applyFix(Project project, ProblemDescriptor descriptor) {
        createJsVariable(project, descriptor.getPsiElement());
    }

    private void createJsVariable(Project project, PsiElement psiElement) {
        if (!Application.get().isDispatchThread()) {
            return;
        }

        HttpRequestBlock requestBlock;
        if (psiElement instanceof HttpVariableName) {
            requestBlock = PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock.class);
        } else if (psiElement instanceof JsonStringLiteral) {
            PsiElement element = InjectedLanguageManager.getInstance(project).getInjectionHost(psiElement);
            requestBlock = PsiTreeUtil.getParentOfType(element, HttpRequestBlock.class);
        } else {
            return;
        }

        if (requestBlock == null) {
            return;
        }

        Editor textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (textEditor == null) {
            return;
        }
        HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());

        HttpScriptBody scriptBody;
        if (global) {
            HttpGlobalHandler globalHandler = httpFile.getGlobalHandler();
            if (globalHandler != null && globalHandler.getGlobalScript() != null) {
                scriptBody = globalHandler.getGlobalScript().getScriptBody();
            } else {
                scriptBody = null;
            }
        } else {
            HttpPreRequestHandler preRequestHandler = requestBlock.getPreRequestHandler();
            if (preRequestHandler != null && preRequestHandler.getPreRequestScript() != null) {
                scriptBody = preRequestHandler.getPreRequestScript().getScriptBody();
            } else {
                scriptBody = null;
            }
        }

        if (scriptBody == null) {
            createAndAddHandler(project, httpFile, requestBlock);
            return;
        }

        var injectedPsiFiles = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(scriptBody);
        if (injectedPsiFiles == null || injectedPsiFiles.isEmpty()) {
            return;
        }
        PsiFile injectedPsiFile = (PsiFile) injectedPsiFiles.get(0).getFirst();
        if (injectedPsiFile == null) {
            return;
        }

        JsFacade.createJsVariable(project, injectedPsiFile, variableName);
    }

    private void createAndAddHandler(Project project, HttpFile httpFile, org.javamaster.httpclient.psi.HttpRequestBlock requestBlock) {
        String handler;
        if (global) {
            handler = "<! {%\n" +
                    "    request.variables.set('" + variableName + "', '');\n" +
                    "%}\n" +
                    "\n" +
                    "\n";
        } else {
            handler = "###\n" +
                    "< {%\n" +
                    "    request.variables.set('" + variableName + "', '');\n" +
                    "%}\n" +
                    "GET";
        }

        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        HttpFile tmpFile = (HttpFile) psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, handler);

        HttpScriptBody scriptBody;
        if (global) {
            HttpGlobalHandler newGlobalHandler = PsiTreeUtil.findChildOfType(tmpFile, HttpGlobalHandler.class);
            if (newGlobalHandler == null) {
                return;
            }

            List<HttpDirectionComment> directionComments = httpFile.getDirectionComments();
            HttpGlobalHandler globalHandler;
            if (!directionComments.isEmpty()) {
                globalHandler = (HttpGlobalHandler) httpFile.addAfter(newGlobalHandler, directionComments.get(directionComments.size() - 1).getNextSibling());
            } else {
                globalHandler = (HttpGlobalHandler) httpFile.addBefore(newGlobalHandler, httpFile.getFirstChild());
            }

            if (globalHandler.getGlobalScript() == null || globalHandler.getGlobalScript().getScriptBody() == null) {
                return;
            }
            scriptBody = globalHandler.getGlobalScript().getScriptBody();
        } else {
            HttpRequest request = requestBlock.getRequest();
            List<HttpRequestBlock> requestBlocks = tmpFile.getRequestBlocks();
            if (requestBlocks.isEmpty() || requestBlocks.get(0).getPreRequestHandler() == null) {
                return;
            }
            HttpPreRequestHandler newPreRequestHandler = requestBlocks.get(0).getPreRequestHandler();
            HttpPreRequestHandler preRequestHandler = (HttpPreRequestHandler) requestBlock.addBefore(newPreRequestHandler, request);
            if (preRequestHandler.getPreRequestScript() == null || preRequestHandler.getPreRequestScript().getScriptBody() == null) {
                return;
            }
            scriptBody = preRequestHandler.getPreRequestScript().getScriptBody();
        }

        // Move the cursor into the quotation marks
        PsiElement jsVariable = JsFacade.resolveJsVariable(variableName, project, Collections.singletonList(scriptBody));
        if (jsVariable == null) {
            return;
        }
        ((Navigatable) jsVariable.getParent().getLastChild().getPrevSibling()).navigate(true);
        CaretModel caretModel = FileEditorManager.getInstance(project).getSelectedTextEditor() != null ?
                FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel() : null;
        if (caretModel == null) {
            return;
        }
        caretModel.moveToOffset(caretModel.getOffset() + 1);
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }
}
