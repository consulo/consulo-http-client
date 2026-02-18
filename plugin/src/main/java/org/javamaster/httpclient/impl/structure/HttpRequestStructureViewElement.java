package org.javamaster.httpclient.impl.structure;

import consulo.codeEditor.CodeInsightColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.component.util.Iconable;
import consulo.document.util.TextRange;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.restClient.icon.RestClientIconGroup;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpContentType;
import org.javamaster.httpclient.psi.HttpMessageBody;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.Nullable;

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

    private final String myPresentationText;
    private final String myLocation;
    private final Image myIcon;
    private final boolean myIsValid;

    private HttpRequestStructureViewElement(
        PsiElement element,
        String presentationText,
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
    public Collection<StructureViewTreeElement> getChildrenBase() {
        PsiElement element = getElement();
        if (element instanceof HttpFile) {
            HttpFile httpFile = (HttpFile) element;
            List<HttpRequestBlock> blocks = httpFile.getRequestBlocks();
            if (blocks.isEmpty()) {
                return Collections.emptyList();
            }

            List<StructureViewTreeElement> children = new ArrayList<>();
            var globalHandler = httpFile.getGlobalHandler();
            if (globalHandler != null) {
                children.add(create(globalHandler, NlsBundle.message("global.handler"), RestClientIconGroup.playfirst()));
            }

            var globalVariables = httpFile.getGlobalVariables();
            for (var globalVariable : globalVariables) {
                children.add(
                    create(
                        globalVariable,
                        NlsBundle.message("global.variable"),
                        globalVariable.getText(),
                        PlatformIconGroup.generalInlinevariables(),
                        true
                    )
                );
            }

            for (HttpRequestBlock block : blocks) {
                List<StructureViewTreeElement> list = getChildren(block);
                children.addAll(list);
            }
            return children;
        }
        else if (element instanceof HttpRequestBlock) {
            return getChildren((HttpRequestBlock) element);
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
    public String getPresentableText() {
        return myPresentationText;
    }

    @Nullable
    @Override
    public Image getIcon() {
        return myIcon != null ? myIcon : super.getIcon();
    }

    public static StructureViewTreeElement create(PsiElement element, String text, Image icon) {
        return create(element, text, null, icon);
    }

    public static StructureViewTreeElement create(
        PsiElement element,
        String text,
        String location,
        Image icon
    ) {
        return new HttpRequestStructureViewElement(
            element,
            StringUtil.notNullize(text, NlsBundle.message("not.defined")),
            location,
            icon,
            StringUtil.isNotEmpty(text)
        );
    }

    public static StructureViewTreeElement create(
        PsiElement element,
        String text,
        String location,
        Image icon,
        boolean isValid
    ) {
        return new HttpRequestStructureViewElement(element, text, location, icon, isValid);
    }

    private static List<StructureViewTreeElement> getChildren(HttpRequestBlock block) {
        List<StructureViewTreeElement> children = new ArrayList<>();
        var request = block.getRequest();
        var originalHost = request.getHttpHost();
        var target = request.getRequestTarget();
        var path = target != null ? target.getUrl() : null;
        Project project = block.getProject();

        var preRequestHandler = block.getPreRequestHandler();
        if (preRequestHandler != null) {
            children.add(create(preRequestHandler, NlsBundle.message("pre.handler"), RestClientIconGroup.playfirst()));
        }

        StringBuilder location = new StringBuilder();
        location.append(StringUtil.notNullize(path, NlsBundle.message("not.defined")));

        String tabName;
        var method = request.getMethod();
        tabName = HttpUtilsPart.getTabName(method);

        Image icon = HttpUtilsPart.pickMethodIcon(method.getText());

        children.add(
            create(request, tabName, location.toString(), icon, StringUtil.isNotEmpty(originalHost))
        );

        var body = request.getBody();
        var messagesGroup = body != null ? body.getRequestMessagesGroup() : null;
        if (messagesGroup != null) {
            String mimeType = NlsBundle.message("not.defined");
            HttpContentType contentType = request.getContentType();
            if (contentType != null) {
                mimeType = contentType.mimeType();
            }

            Image bodyIcon = contentType != null && isImageType(contentType.mimeType()) ?
                HttpIcons.IMAGE :
                getInjectedLanguageIcon(project, messagesGroup.getMessageBody());

            children.add(create(messagesGroup, NlsBundle.message("request.body") + " " + mimeType, bodyIcon));
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

                String mimeType = NlsBundle.message("not.defined");
                HttpContentType contentType = multipartField.getContentType();
                if (contentType != null) {
                    mimeType = contentType.mimeType();
                }

                Image multiIcon = isImageType(contentType.mimeType()) ?
                    HttpIcons.IMAGE :
                    getInjectedLanguageIcon(project, multipartField.getRequestMessagesGroup().getMessageBody());

                children.add(create(multipartField, NlsBundle.message("multipart.field") + ": " + name + " " + mimeType, multiIcon));
            }
        }

        var responseHandler = request.getResponseHandler();
        if (responseHandler != null) {
            children.add(create(responseHandler, NlsBundle.message("response.handler"), RestClientIconGroup.playfirst()));
        }

        return children;
    }

    private static boolean isImageType(String contentType) {
        return contentType != null && imageContentSet.contains(contentType);
    }

    @Nullable
    private static Image getInjectedLanguageIcon(Project project, HttpMessageBody messageBody) {
        if (messageBody == null) {
            return null;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
        List<Pair<PsiElement, TextRange>> files =
            injectedLanguageManager.getInjectedPsiFiles(messageBody);
        if (files == null || files.isEmpty()) {
            return null;
        }

        PsiElement psiElement = files.get(0).getFirst();
        PsiFile psiFile = psiElement instanceof PsiFile ? (PsiFile) psiElement : null;
        return psiFile != null ? IconDescriptorUpdaters.getIcon(psiFile, Iconable.ICON_FLAG_VISIBILITY) : null;
    }
}
