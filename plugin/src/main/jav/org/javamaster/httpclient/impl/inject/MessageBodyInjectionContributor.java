package org.javamaster.httpclient.impl.inject;

import consulo.json.lang.JsonLanguage;
import consulo.language.Language;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.plain.PlainTextLanguage;
import consulo.document.util.TextRange;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.util.collection.SmartList;
import consulo.util.collection.ContainerUtil;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.impl.action.dashboard.view.ContentTypeActionGroup;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpMultipartField;
import org.javamaster.httpclient.psi.HttpPsiUtils;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.utils.InjectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yudong
 */
public class MessageBodyInjectionContributor implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        VirtualFile virtualFile = PsiUtil.getVirtualFile(context);
        PsiLanguageInjectionHost host = (PsiLanguageInjectionHost) context;

        if (virtualFile != null) {
            ContentType contentType = virtualFile.getUserData(ContentTypeActionGroup.httpDashboardContentTypeKey);
            if (contentType != null) {
                tryInject(contentType, host, registrar);
                return;
            }
        }

        ContentType contentType = null;
        PsiElement tmpParent = context.getParent().getParent();
        PsiElement parent = tmpParent != null ? tmpParent.getParent() : null;
        if (parent instanceof HttpRequest) {
            contentType = ((HttpRequest) parent).getContentType();
        } else if (tmpParent instanceof HttpMultipartField) {
            contentType = ((HttpMultipartField) tmpParent).getContentType();
        }

        tryInject(contentType, host, registrar);
    }

    private void tryInject(ContentType contentType, PsiLanguageInjectionHost host, MultiHostRegistrar registrar) {
        String mimeType = contentType != null ? contentType.getMimeType() : ContentType.TEXT_PLAIN.getMimeType();

        List<Language> languages = Language.findInstancesByMimeType(mimeType);
        Language language = ContainerUtil.getFirstItem(languages);
        if (language == null) {
            language = PlainTextLanguage.INSTANCE;
        }

        if (language == JsonLanguage.INSTANCE) {
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

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return new SmartList<>(HttpMessageBody.class);
    }
}
