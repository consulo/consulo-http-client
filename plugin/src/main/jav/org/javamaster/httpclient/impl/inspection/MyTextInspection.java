package org.javamaster.httpclient.impl.inspection;

import consulo.language.editor.inspection.InspectionManager;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.impl.inspection.support.InspectionHelper;

import java.util.Collections;

/**
 * @author yudong
 */
public class MyTextInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly) {
        return InspectionHelper.checkVariables(Collections.singletonList(file), manager);
    }
}
