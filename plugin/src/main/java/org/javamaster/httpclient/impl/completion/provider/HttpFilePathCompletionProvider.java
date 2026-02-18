package org.javamaster.httpclient.impl.completion.provider;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import org.javamaster.httpclient.impl.completion.support.SlashInsertHandler;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.psi.*;

import java.io.File;

/**
 * @author yudong
 */
public class HttpFilePathCompletionProvider implements CompletionProvider {

    @Override
    public void addCompletions(
        CompletionParameters parameters,
        ProcessingContext context,
        CompletionResultSet result
    ) {
        PsiElement psiElement = parameters.getPosition();
        PsiElement parent = psiElement.getParent().getParent();
        PsiElement parentParent = parent.getParent();

        if (parentParent instanceof HttpDirectionComment) {
            HttpDirectionName directionName = ((HttpDirectionComment) parentParent).getDirectionName();
            if (!ParamEnum.isFilePathParam(directionName != null ? directionName.getText() : null)) {
                return;
            }
        }

        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(psiElement);
        if (virtualFile == null) {
            return;
        }

        HttpVariable variable = findVariable(parent);
        if (variable != null) {
            HttpVariableName variableName = variable.getVariableName();
            if (variableName == null) {
                return;
            }

            var psiFile = PsiUtilCore.getPsiFile(parentParent.getProject(), virtualFile);
// TODO !
//            PsiElement psiDirectory = HttpVariableNamePsiReference.tryResolveVariable(
//                variableName.getName(),
//                variableName.isBuiltin(),
//                psiFile,
//                false
//            );
//
//            if (!(psiDirectory instanceof PsiDirectory)) {
//                return;
//            }
//
//            fillRootPaths((PsiDirectory) psiDirectory, variable.getText(), result);
        } else {
            VirtualFile root = virtualFile.getParent();
            if (root == null) {
                return;
            }

            for (File file : File.listRoots()) {
                result.addElement(LookupElementBuilder.create(file, file.getPath()));
            }

            fillRootPaths(root, result);
        }
    }

    private void fillRootPaths(PsiDirectory psiDirectory, String variableText, CompletionResultSet result) {
        String prefix = result.getPrefixMatcher().getPrefix();
        VirtualFile root = psiDirectory.getVirtualFile();

        int start = variableText.length();
        int end = prefix.lastIndexOf("/");
        if (end == -1 || end < start) {
            end = start;
        }

        String path = prefix.substring(start, end);

        VirtualFile relativeFile = VirtualFileUtil.findFileByIoFile(new File(root.getPath() + path), false);

        String prefixPath;
        VirtualFile virtualFile;
        if (relativeFile != null) {
            prefixPath = path;
            virtualFile = relativeFile;
        } else {
            prefixPath = "";
            virtualFile = root;
        }

        fillPaths(virtualFile, variableText, prefixPath, result);
    }

    private void fillPaths(
        VirtualFile virtualFile,
        String variableText,
        String prefixPath,
        CompletionResultSet result
    ) {
        for (VirtualFile file : virtualFile.getChildren()) {
            if (file.getName().startsWith(".")) {
                continue;
            }

            String relativize = variableText + prefixPath + file.getPath().substring(virtualFile.getPath().length());
            if (file.isDirectory()) {
                result.addElement(LookupElementBuilder.create(file, relativize).withInsertHandler(SlashInsertHandler.INSTANCE));
            } else {
                result.addElement(LookupElementBuilder.create(file, relativize));
            }
        }
    }

    private void fillRootPaths(VirtualFile root, CompletionResultSet result) {
        int[] num = {0};

        VirtualFileUtil.iterateChildrenRecursively(root, null, fileOrDir -> {
            num[0]++;
            String relativize = fileOrDir.getPath().substring(root.getPath().length() + 1);
            if (fileOrDir.isDirectory()) {
                result.addElement(LookupElementBuilder.create(fileOrDir, relativize).withInsertHandler(SlashInsertHandler.INSTANCE));
            } else {
                result.addElement(LookupElementBuilder.create(fileOrDir, relativize));
            }

            return num[0] <= 600;
        }, VirtualFileVisitor.SKIP_ROOT);
    }

    private HttpVariable findVariable(PsiElement parent) {
        HttpVariable variable = null;
        if (parent instanceof HttpFilePath) {
            variable = ((HttpFilePath) parent).getVariable();
        } else if (parent instanceof HttpDirectionValue) {
            variable = ((HttpDirectionValue) parent).getVariable();
        }

        return variable;
    }
}
