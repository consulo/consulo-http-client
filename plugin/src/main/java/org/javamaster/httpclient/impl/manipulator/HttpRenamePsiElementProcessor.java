package org.javamaster.httpclient.impl.manipulator;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import org.javamaster.httpclient.psi.HttpGlobalVariableName;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpRenamePsiElementProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(PsiElement element) {
        return element instanceof HttpGlobalVariableName;
    }

}
