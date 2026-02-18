package org.javamaster.httpclient.impl.action.dashboard.view;

import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.util.FileContentUtilCore;
import consulo.language.psi.PsiDocumentManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.awt.action.CustomComponentAction;
import consulo.util.dataholder.Key;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.parser.HttpFile;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yudong
 */
public class ContentTypeActionGroup {
    public static final Key<ContentType> httpDashboardContentTypeKey = Key.create("org.javamaster.dashboard.httpDashboardContentType");

    private final Editor editor;
    private final ContentTypeAction textAction;
    private final ContentTypeAction jsonAction;
    private final ContentTypeAction xmlAction;
    private final ContentTypeAction htmlAction;
    private final List<ContentTypeAction> actions;
    private final Set<ContentType> allowContentTypes;
    private ContentType contentType;

    public ContentTypeActionGroup(Editor editor) {
        this.editor = editor;
        this.textAction = new TextAction(new HashSet<>(Arrays.asList(ContentType.TEXT_PLAIN)));
        this.jsonAction = new JsonAction(new HashSet<>(Arrays.asList(ContentType.APPLICATION_JSON)));
        this.xmlAction = new XmlAction(new HashSet<>(Arrays.asList(ContentType.TEXT_XML, ContentType.APPLICATION_XML)));
        this.htmlAction = new HtmlAction(new HashSet<>(Arrays.asList(ContentType.TEXT_HTML, ContentType.APPLICATION_XHTML_XML)));

        this.actions = Arrays.asList(textAction, jsonAction, xmlAction, htmlAction);

        this.allowContentTypes = new HashSet<>();
        allowContentTypes.addAll(textAction.relateTypes);
        allowContentTypes.addAll(jsonAction.relateTypes);
        allowContentTypes.addAll(xmlAction.relateTypes);
        allowContentTypes.addAll(htmlAction.relateTypes);

        this.contentType = calContentType();
    }

    public List<ContentTypeAction> getActions() {
        return actions;
    }

    private ContentType calContentType() {
        Project project = editor.getProject();
        Document document = editor.getDocument();
        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (!(psiFile instanceof HttpFile)) {
            return null;
        }

        HttpFile httpFile = (HttpFile) psiFile;
        var requestBlocks = httpFile.getRequestBlocks();
        if (requestBlocks.isEmpty()) {
            return null;
        }

        ContentType contentType = requestBlocks.get(0).getRequest().getContentType();
        if (contentType == null) {
            return null;
        }

        if (!allowContentTypes.contains(contentType)) {
            return null;
        }

        return contentType;
    }

    private void changeActionButtons(ContentType contentType) {
        this.contentType = contentType;

        actions.forEach(action -> action.changeActionButton(contentType));
    }

    public abstract class ContentTypeAction extends AnAction implements CustomComponentAction {
        protected final Set<ContentType> relateTypes;
        private ActionButtonWithText actionButtonWithText;

        public ContentTypeAction(Set<ContentType> relateTypes, String text) {
            super(text, null, null);
            this.relateTypes = relateTypes;
        }

        @Override
        public JComponent createCustomComponent(Presentation presentation, String place) {
            actionButtonWithText = new ActionButtonWithText(this, presentation, place, new Dimension(20, 20));

            changeActionButton(contentType);

            return actionButtonWithText;
        }

        public void changeActionButton(ContentType contentType) {
            if (contentType == null) {
                actionButtonWithText.setEnabled(false);
                actionButtonWithText.getPresentation().setIcon(HttpIcons.BLANK);
                return;
            }

            if (relateTypes.contains(contentType)) {
                actionButtonWithText.getPresentation().setIcon(PlatformIconGroup.actionsChecked);
            } else {
                actionButtonWithText.getPresentation().setIcon(HttpIcons.BLANK);
            }
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = editor.getProject();
            Document document = editor.getDocument();
            var httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if (!(httpFile instanceof HttpFile)) {
                return;
            }

            if (ContentTypeActionGroup.this.contentType == null) {
                return;
            }

            ContentType contentType = relateTypes.iterator().next();

            httpFile.getVirtualFile().putUserData(httpDashboardContentTypeKey, contentType);

            changeActionButtons(contentType);

            FileContentUtilCore.reparseFiles(httpFile.getVirtualFile());
        }
    }

    private class TextAction extends ContentTypeAction {
        public TextAction(Set<ContentType> relateTypes) {
            super(relateTypes, "Text");
        }
    }

    private class JsonAction extends ContentTypeAction {
        public JsonAction(Set<ContentType> relateTypes) {
            super(relateTypes, "JSON");
        }
    }

    private class XmlAction extends ContentTypeAction {
        public XmlAction(Set<ContentType> relateTypes) {
            super(relateTypes, "XML");
        }
    }

    private class HtmlAction extends ContentTypeAction {
        public HtmlAction(Set<ContentType> relateTypes) {
            super(relateTypes, "HTML");
        }
    }
}
