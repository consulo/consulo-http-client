package org.javamaster.httpclient.impl.inspection;

import com.intellij.json.psi.JsonStringLiteral;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.javamaster.httpclient.impl.inspection.support.InspectionHelper;

import java.util.Collection;

public class MyJsonInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly) {
        Collection<JsonStringLiteral> jsonStringLiterals = PsiTreeUtil.findChildrenOfType(file, JsonStringLiteral.class);

        return InspectionHelper.checkVariables(jsonStringLiterals, manager);
    }
}
