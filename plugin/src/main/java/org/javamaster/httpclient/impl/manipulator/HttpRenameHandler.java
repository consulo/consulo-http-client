package org.javamaster.httpclient.impl.manipulator;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.psi.HttpGlobalVariableName;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpRenameHandler implements RenameHandler {

    @Override
    public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
        if (editor == null) {
            return;
        }

        Integer offset = editor.getCaretModel().getCurrentCaret().getOffset();
        if (offset == null) {
            return;
        }

        if (file == null) {
            return;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null) {
            return;
        }

        PsiElement parent = psiElement.getParent();
        RenamePsiElementProcessor processor = RenamePsiElementProcessor.forElement(parent);

        // 以后可以考虑实现成类似重命名类名那样的效果,不弹出弹窗,这样用户体验更好
        processor.createRenameDialog(project, parent, parent, editor).show();
    }

    @Override
    public void invoke(Project project, PsiElement[] elements, DataContext dataContext) {

    }

    @Override
    public boolean isAvailableOnDataContext(DataContext dataContext) {
        Editor editor = dataContext.getData(Editor.KEY);
        if (editor == null) {
            return false;
        }
        PsiElement psiElement = PsiUtilBase.getElementAtCaret(editor);

        return psiElement != null && psiElement.getParent() instanceof HttpGlobalVariableName;
    }

    @Override
    public boolean isRenaming(DataContext dataContext) {
        return isAvailableOnDataContext(dataContext);
    }

    @Nonnull
    @Override
    public LocalizeValue getActionTitleValue() {
        return LocalizeValue.localizeTODO("Rename");
    }

}
