package org.javamaster.httpclient.impl.inject;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.plain.PlainTextLanguage;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiUtilCore;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.utils.InjectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author yudong
 */
@ExtensionImpl
public class MessageBodyInjectionContributor implements MultiHostInjector {

    @Override
    public void injectLanguages(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(context);
        PsiLanguageInjectionHost host = (PsiLanguageInjectionHost) context;

        // TODO
//        if (virtualFile != null) {
//            ContentType contentType = virtualFile.getUserData(ContentTypeActionGroup.httpDashboardContentTypeKey);
//            if (contentType != null) {
//                tryInject(contentType, host, registrar);
//                return;
//            }
//        }

        HttpContentType contentType = null;
        PsiElement tmpParent = context.getParent().getParent();
        PsiElement parent = tmpParent != null ? tmpParent.getParent() : null;
        if (parent instanceof HttpRequest) {
            contentType = ((HttpRequest) parent).getContentType();
        } else if (tmpParent instanceof HttpMultipartField) {
            contentType = ((HttpMultipartField) tmpParent).getContentType();
        }

        tryInject(contentType, host, registrar);
    }

    private void tryInject(HttpContentType contentType, PsiLanguageInjectionHost host, MultiHostRegistrar registrar) {
        String mimeType = contentType != null ? contentType.mimeType() : "text/plain";

        Collection<Language> languages = Language.findInstancesByMimeType(mimeType);
        Language language = ContainerUtil.getFirstItem(languages);
        if (language == null) {
            language = PlainTextLanguage.INSTANCE;
        }

        if ("JSON".equals(language.getID())) {
            registrar.startInjecting(language);
            String text = host.getText();

            List<TextRange> variableRanges = injectBody(registrar, host, text);

            registrar.doneInjecting();

            if (!variableRanges.isEmpty()) {
                registrar.startInjecting(PlainTextLanguage.INSTANCE);

                for (TextRange range : variableRanges) {
                    registrar.addPlace(null, null, host, range);
                }

                registrar.doneInjecting();
            }
        } else {
            TextRange textRange = InjectionUtils.innerRange(host);
            if (textRange == null) {
                return;
            }

            registrar.startInjecting(language);
            registrar.addPlace(null, null, host, textRange);
            registrar.doneInjecting();
        }
    }

    private List<TextRange> injectBody(
            MultiHostRegistrar registrar,
            PsiLanguageInjectionHost host,
            String messageText
    ) {
        int lastVariableRangeEndOffset = 0;

        List<TextRange> variablesRanges = HttpPsiUtils.collectVariablesRangesInMessageBody(messageText);

        for (TextRange variableRange : variablesRanges) {
            TextRange range = TextRange.create(lastVariableRangeEndOffset, variableRange.getStartOffset());

            registrar.addPlace(null, "0", host, range);

            lastVariableRangeEndOffset = variableRange.getEndOffset();
        }

        TextRange range = TextRange.create(lastVariableRangeEndOffset, messageText.length());

        registrar.addPlace(null, null, host, range.shiftRight(0));

        return variablesRanges;
    }

    @Nonnull
    @Override
    public Class<? extends PsiElement> getElementClass() {
        return HttpMessageBody.class;
    }
}
