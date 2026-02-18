package org.javamaster.httpclient.impl.gutter.support;

import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.document.util.TextRange;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.psi.PsiElement;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author yudong
 */
public class HttpLineMarkerInfo extends LineMarkerInfo<PsiElement> {
    public HttpLineMarkerInfo(
            @NotNull PsiElement element,
            @NotNull TextRange range,
            @NotNull Image icon,
            @NotNull Function<PsiElement, String> tooltipProvider,
            @NotNull GutterIconNavigationHandler<PsiElement> navHandler,
            @NotNull GutterIconRenderer.Alignment alignment,
            @NotNull Supplier<String> accessibleNameProvider
    ) {
        super(element, range, icon, Pass.LINE_MARKERS, tooltipProvider, navHandler, alignment);
    }
}
