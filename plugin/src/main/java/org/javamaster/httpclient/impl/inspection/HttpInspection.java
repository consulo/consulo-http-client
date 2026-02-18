package org.javamaster.httpclient.impl.inspection;

import consulo.language.editor.inspection.InspectionManager;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.impl.inspection.support.InspectionHelper;
import org.javamaster.httpclient.psi.HttpVariableName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yudong
 */
public class HttpInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly) {
        List<PsiElement> variables = new ArrayList<>();

        PsiTreeUtil.processElements(file, it -> {
            if (it instanceof HttpVariableName) {
                variables.add(it);
            }
            return true;
        });

        return InspectionHelper.checkVariables(variables, manager);
    }
}
