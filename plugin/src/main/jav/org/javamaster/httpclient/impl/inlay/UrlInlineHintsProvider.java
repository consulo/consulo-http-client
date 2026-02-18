package org.javamaster.httpclient.impl.inlay;

import consulo.language.editor.inlay.InlayHintsCollector;
import consulo.language.editor.inlay.InlayHintsProvider;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.java.language.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class UrlInlineHintsProvider implements InlayHintsProvider {

    @Nullable
    @Override
    public InlayHintsCollector createCollector(@NotNull PsiFile file, @NotNull Editor editor) {
        if (!(file instanceof PsiJavaFile)) {
            return null;
        }

        return new HttpInlayHintsCollector();
    }
}
