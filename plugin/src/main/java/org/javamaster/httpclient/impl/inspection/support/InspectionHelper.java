package org.javamaster.httpclient.impl.inspection.support;

import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.PriorityAction;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.inspection.fix.CreateEnvVariableQuickFix;
import org.javamaster.httpclient.impl.inspection.fix.CreateFileVariableQuickFix;
import org.javamaster.httpclient.impl.inspection.fix.CreateJsVariableQuickFix;
import org.javamaster.httpclient.impl.jsPlugin.JsFacade;
import org.javamaster.httpclient.impl.reference.support.HttpVariableNamePsiReference;
import org.javamaster.httpclient.impl.reference.support.TextVariableNamePsiReference;
import org.javamaster.httpclient.psi.HttpVariableName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InspectionHelper {

    public static ProblemDescriptor[] checkVariables(
            Collection<? extends PsiElement> psiElements,
            InspectionManager manager
    ) {
        List<ProblemDescriptor> list = new ArrayList<>();

        for (PsiElement element : psiElements) {
            for (PsiReference reference : element.getReferences()) {
                boolean builtin;
                TextRange textRange;
                String variableName;

                if (reference instanceof HttpVariableNamePsiReference) {
                    HttpVariableNamePsiReference httpRef = (HttpVariableNamePsiReference) reference;
                    textRange = httpRef.getTextRange();
                    builtin = httpRef.getElement().isBuiltin();
                    variableName = httpRef.getElement().getName();
                } else if (reference instanceof TextVariableNamePsiReference) {
                    TextVariableNamePsiReference textRef = (TextVariableNamePsiReference) reference;
                    textRange = textRef.getTextRange().shiftLeft(element.getTextRange().getStartOffset());
                    HttpVariableName varName = textRef.getVariable().getVariableName();
                    builtin = varName == null || varName.isBuiltin();
                    variableName = varName != null ? varName.getName() : null;
                } else {
                    builtin = true;
                    textRange = null;
                    variableName = null;
                }

                if (variableName == null) {
                    continue;
                }

                if (builtin) {
                    continue;
                }

                if (textRange != null && textRange.getStartOffset() == textRange.getEndOffset()) {
                    continue;
                }

                PsiElement resolve = reference.resolve();

                if (resolve != null) {
                    continue;
                }

                List<LocalQuickFix> fixes = new ArrayList<>();
                fixes.add(new CreateEnvVariableQuickFix(false, variableName, PriorityAction.Priority.TOP));
                fixes.add(new CreateEnvVariableQuickFix(true, variableName, PriorityAction.Priority.HIGH));

                if (reference instanceof HttpVariableNamePsiReference) {
                    if (JsFacade.isAvailable()) {
                        fixes.add(new CreateJsVariableQuickFix(true, variableName));
                        fixes.add(new CreateJsVariableQuickFix(false, variableName));
                    }
                }

                fixes.add(new CreateFileVariableQuickFix(variableName));

                ProblemDescriptor problem = manager.createProblemDescriptor(
                        element,
                        textRange,
                        NlsBundle.message("variable.unresolved", variableName),
                        ProblemHighlightType.WARNING,
                        true,
                        fixes.toArray(new LocalQuickFix[0])
                );
                list.add(problem);
            }
        }

        return list.toArray(new ProblemDescriptor[0]);
    }
}
