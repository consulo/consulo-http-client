package org.javamaster.httpclient.impl.gutter;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.impl.gutter.support.HttpDiffGutterIconNavigationHandler;
import org.javamaster.httpclient.impl.gutter.support.HttpGutterIconNavigationHandler;
import org.javamaster.httpclient.impl.gutter.support.HttpLineMarkerInfo;
import org.javamaster.httpclient.psi.HttpHistoryBodyFile;
import org.javamaster.httpclient.psi.HttpHistoryBodyFileList;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpTypes;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpLineMarkerProvider implements LineMarkerProvider {
    private final Function<PsiElement, String> tooltipProvider = (element) -> HttpClientLocalize.sendRequest().get();
    private final Supplier<String> accessibleNameProvider = () -> HttpClientLocalize.sendRequest().get();
    private final String tip = HttpClientLocalize.compareWith().get() + "...";
    private final Function<PsiElement, String> tooltipCompareProvider = (element) -> tip;
    private final Supplier<String> accessibleNameCompareProvider = () -> tip;

    @RequiredReadAction
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        IElementType elementType = element.getNode().getElementType();

        if (elementType == HttpTypes.REQUEST_METHOD) {
            return createRunIconInfo(element);
        }

        if (elementType == HttpTypes.HISTORY_FILE_SIGN) {
            return createDiffIconInfo(element);
        }

        return null;
    }

    @Nullable
    private HttpLineMarkerInfo createRunIconInfo(@NotNull PsiElement element) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof HttpMethod)) {
            return null;
        }

        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
        if (HttpUtilsPart.isFileInIdeaDir(virtualFile)) {
            return null;
        }

        return new HttpLineMarkerInfo(
                element,
                element.getTextRange(),
                PlatformIconGroup.actionsExecute(),
                tooltipProvider,
                HttpGutterIconNavigationHandler.INSTANCE,
                GutterIconRenderer.Alignment.CENTER,
                accessibleNameProvider
        );
    }

    @Nullable
    private HttpLineMarkerInfo createDiffIconInfo(@NotNull PsiElement element) {
        HttpHistoryBodyFile historyBodyFile = (HttpHistoryBodyFile) element.getParent();
        HttpHistoryBodyFileList bodyFileList = (HttpHistoryBodyFileList) historyBodyFile.getParent();

        if (historyBodyFile.getFilePath() == null || bodyFileList.getHistoryBodyFileList().size() <= 1) {
            return null;
        }

        return new HttpLineMarkerInfo(
                element,
                element.getTextRange(),
                PlatformIconGroup.actionsDiff(),
                tooltipCompareProvider,
                HttpDiffGutterIconNavigationHandler.INSTANCE,
                GutterIconRenderer.Alignment.CENTER,
                accessibleNameCompareProvider
        );
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
