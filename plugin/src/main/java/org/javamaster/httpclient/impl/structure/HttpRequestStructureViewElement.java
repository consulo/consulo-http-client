package org.javamaster.httpclient.impl.structure;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.CodeInsightColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.component.util.Iconable;
import consulo.document.util.TextRange;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.httpClient.icon.HttpClientIconGroup;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpContentType;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class HttpRequestStructureViewElement extends PsiTreeElementBase<PsiElement> implements ColoredItemPresentation {
    private static final Set<String> imageContentSet = new HashSet<>(Arrays.asList(
        "image/png",
        "image/jpeg",
        "image/bmp",
        "image/webp",
        "image/svg+xml",
        "image/gif",
        "image/tiff"
    ));

    private final LocalizeValue myPresentationText;
    private final String myLocation;
    private final Image myIcon;
    private final boolean myIsValid;

    private HttpRequestStructureViewElement(
        PsiElement element,
        LocalizeValue presentationText,
        String location,
        Image icon,
        boolean isValid
    ) {
        super(element);
        myPresentationText = presentationText;
        myLocation = location;
        myIcon = icon;
        myIsValid = isValid;
    }

    @Override
    @RequiredReadAction
    public Collection<StructureViewTreeElement> getChildrenBase() {
        PsiElement element = getElement();
        if (element instanceof HttpFile httpFile) {
            List<HttpRequestBlock> blocks = httpFile.getRequestBlocks();
            if (blocks.isEmpty()) {
                return Collections.emptyList();
            }

            List<StructureViewTreeElement> children = new ArrayList<>();
            var globalHandler = httpFile.getGlobalHandler();
            if (globalHandler != null) {
                children.add(create(globalHandler, HttpClientLocalize.globalHandler(), HttpClientIconGroup.playfirst()));
            }

            var globalVariables = httpFile.getGlobalVariables();
            for (var globalVariable : globalVariables) {
                children.add(create(
                    globalVariable,
                    HttpClientLocalize.globalVariable(),
                    globalVariable.getText(),
                    PlatformIconGroup.generalInlinevariables(),
                    true
                ));
            }

            for (HttpRequestBlock block : blocks) {
                List<StructureViewTreeElement> list = getChildren(block);
                children.addAll(list);
            }
            return children;
        }
        else if (element instanceof HttpRequestBlock requestBlock) {
            return getChildren(requestBlock);
        }

        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return myLocation;
    }

    @Override
    public boolean isSearchInLocationString() {
        return true;
    }

    @Nullable
    @Override
    public TextAttributesKey getTextAttributesKey() {
        return myIsValid ? null : CodeInsightColors.ERRORS_ATTRIBUTES;
    }

    @Override
    public LocalizeValue getPresentableText() {
        return myPresentationText;
    }

    @Override
    @RequiredReadAction
    public @Nullable Image getIcon() {
        return myIcon != null ? myIcon : super.getIcon();
    }

    public static StructureViewTreeElement create(PsiElement element, LocalizeValue text, Image icon) {
        return create(element, text, null, icon);
    }

    public static StructureViewTreeElement create(PsiElement element, LocalizeValue text, String location, Image icon) {
        return new HttpRequestStructureViewElement(
            element,
            text.orIfEmpty(HttpClientLocalize.notDefined()),
            location,
            icon,
            text.isNotEmpty()
        );
    }

    public static StructureViewTreeElement create(
        PsiElement element,
        LocalizeValue text,
        String location,
        Image icon,
        boolean isValid
    ) {
        return new HttpRequestStructureViewElement(element, text, location, icon, isValid);
    }

    @RequiredReadAction
    private static List<StructureViewTreeElement> getChildren(HttpRequestBlock block) {
        List<StructureViewTreeElement> children = new ArrayList<>();
        var request = block.getRequest();
        var originalHost = request.getHttpHost();
        var target = request.getRequestTarget();
        var path = target != null ? target.getUrl() : null;
        Project project = block.getProject();

        var preRequestHandler = block.getPreRequestHandler();
        if (preRequestHandler != null) {
            children.add(create(preRequestHandler, HttpClientLocalize.preHandler(), HttpClientIconGroup.playfirst()));
        }

        StringBuilder location = new StringBuilder();
        location.append(StringUtil.notNullize(path, HttpClientLocalize.notDefined().get()));

        HttpMethod method = request.getMethod();
        LocalizeValue tabName = HttpUtilsPart.getTabName(method);

        Image icon = HttpUtilsPart.pickMethodIcon(method.getText());

        children.add(create(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost)));

        var body = request.getBody();
        var messagesGroup = body != null ? body.getRequestMessagesGroup() : null;
        if (messagesGroup != null) {
            LocalizeValue mimeType = HttpClientLocalize.notDefined();
            HttpContentType contentType = request.getContentType();
            if (contentType != null) {
                mimeType = LocalizeValue.of(contentType.mimeType());
            }

            Image bodyIcon = contentType != null && isImageType(contentType.mimeType()) ?
                HttpIcons.IMAGE :
                getInjectedLanguageIcon(project, messagesGroup.getMessageBody());

            children.add(create(
                messagesGroup,
                LocalizeValue.join(HttpClientLocalize.requestBody(), LocalizeValue.space(), mimeType),
                bodyIcon
            ));
        }

        var multipartMessage = body != null ? body.getMultipartMessage() : null;
        if (multipartMessage != null) {
            var multipartFieldList = multipartMessage.getMultipartFieldList();
            for (var multipartField : multipartFieldList) {
                String name = "";
                var headerFieldValue = HttpPsiImplUtil.getMultipartFieldDescription(multipartField);
                if (headerFieldValue != null) {
                    String headerOption = HttpPsiImplUtil.getHeaderFieldOption(headerFieldValue, "name");
                    if (headerOption != null) {
                        name = headerOption;
                    }
                }

                LocalizeValue mimeType = HttpClientLocalize.notDefined();
                HttpContentType contentType = multipartField.getContentType();
                if (contentType != null) {
                    mimeType = LocalizeValue.of(contentType.mimeType());
                }

                Image multiIcon = isImageType(contentType.mimeType())
                    ? HttpIcons.IMAGE
                    : getInjectedLanguageIcon(project, multipartField.getRequestMessagesGroup().getMessageBody());

                children.add(create(
                    multipartField,
                    LocalizeValue.join(
                        HttpClientLocalize.multipartField(),
                        LocalizeValue.of(": "),
                        LocalizeValue.of(name),
                        LocalizeValue.space(),
                        mimeType
                    ),
                    multiIcon
                ));
            }
        }

        var responseHandler = request.getResponseHandler();
        if (responseHandler != null) {
            children.add(create(responseHandler, HttpClientLocalize.responseHandler(), HttpClientIconGroup.playfirst()));
        }

        return children;
    }

    private static boolean isImageType(String contentType) {
        return contentType != null && imageContentSet.contains(contentType);
    }

    @RequiredReadAction
    private static @Nullable Image getInjectedLanguageIcon(Project project, HttpMessageBody messageBody) {
        if (messageBody == null) {
            return null;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
        List<Pair<PsiElement, TextRange>> files =
            injectedLanguageManager.getInjectedPsiFiles(messageBody);
        if (ContainerUtil.isEmpty(files)) {
            return null;
        }

        return files.get(0).getFirst() instanceof PsiFile file ? IconDescriptorUpdaters.getIcon(file, Iconable.ICON_FLAG_VISIBILITY) : null;
    }
}
