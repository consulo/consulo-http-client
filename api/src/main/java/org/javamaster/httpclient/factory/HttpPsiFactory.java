package org.javamaster.httpclient.factory;

import consulo.project.Project;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.HttpFileType;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpGlobalVariable;
import org.javamaster.httpclient.psi.HttpGlobalVariableName;
import org.javamaster.httpclient.psi.HttpVariable;

public class HttpPsiFactory {

    public static HttpGlobalVariableName createGlobalVariableName(Project project, String content) {
        HttpFile psiFile = createDummyFile(project, content);
        return PsiTreeUtil.findChildOfType(psiFile, HttpGlobalVariableName.class);
    }

    public static HttpVariable createVariable(Project project, String content) {
        HttpFile psiFile = createDummyFile(project, content);
        return PsiTreeUtil.findChildOfType(psiFile, HttpVariable.class);
    }

    public static HttpGlobalVariable createGlobalVariable(String variableName, String variableValue, Project project) {
        String txt = "@" + variableName + " = " + variableValue + "\n";

        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);

        HttpFile tmpFile = (HttpFile) psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, txt);
        return PsiTreeUtil.findChildOfType(tmpFile, HttpGlobalVariable.class);
    }

    public static HttpFile createDummyFile(Project project, String content) {
        HttpFileType fileType = HttpFileType.INSTANCE;
        String fileName = "dummy." + fileType.getDefaultExtension();
        return (HttpFile) PsiFileFactory.getInstance(project)
                .createFileFromText(fileName, fileType, content, System.currentTimeMillis(), false);
    }

    private HttpPsiFactory() {
    }
}
