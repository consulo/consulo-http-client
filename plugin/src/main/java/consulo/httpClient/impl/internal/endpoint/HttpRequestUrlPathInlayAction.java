package consulo.httpClient.impl.internal.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.event.EditorMouseEvent;
import consulo.endpoint.client.generator.ClientGenerator;
import consulo.endpoint.client.generator.ClientGeneratorOpenInScratchService;
import consulo.endpoint.url.UrlConstants;
import consulo.endpoint.url.inlay.UrlPathInlayAction;
import consulo.endpoint.url.inlay.UrlPathInlayHint;
import consulo.endpoint.url.reference.UrlPathContext;
import consulo.httpClient.icon.HttpClientIconGroup;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class HttpRequestUrlPathInlayAction implements UrlPathInlayAction {
    @Override
    public Image getIcon() {
        return HttpClientIconGroup.http();
    }

    @Override
    public LocalizeValue getName() {
        return HttpClientLocalize.endpointGenerateRequestAction();
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(PsiFile file, UrlPathInlayHint urlPathInlayHint) {
        List<String> schemes = urlPathInlayHint.getContext().getSchemes();
        if (schemes.isEmpty()) {
            return true;
        }
        for (String scheme : schemes) {
            if (UrlConstants.HTTP_SCHEMES.contains(scheme)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(PsiFile file, Editor editor, UrlPathContext urlPathContext, EditorMouseEvent mouseEvent) {
        ClientGenerator generator = HttpRequestClientGeneratorUtil.find(file.getProject());
        if (generator == null) {
            return;
        }
        ClientGeneratorOpenInScratchService.getInstance(file.getProject()).createScratchFile(generator, editor, urlPathContext);
    }
}
