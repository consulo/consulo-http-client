package org.javamaster.httpclient.impl.action.addHttp;

import consulo.ui.ex.action.AnActionEvent;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.fileEditor.FileDocumentManager;
import consulo.fileEditor.FileEditorManager;
import consulo.ide.impl.idea.openapi.ide.CopyPasteManager;
import consulo.project.Project;
import consulo.ui.ex.awt.InputValidator;
import consulo.ui.ex.awt.Messages;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.curl.CurlParser;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.utils.CurlUtils;
import org.javamaster.httpclient.impl.utils.NotifyUtil;

import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;

/**
 * @author yudong
 */
@SuppressWarnings("ActionPresentationInstantiatedInCtor")
public class ImportCurlAction extends AddAction {
    public ImportCurlAction() {
        super(NlsBundle.message("import.from.curl"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        String contents = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);

        String initialValue = "curl -i https://www.baidu.com";
        if (contents != null && !contents.isEmpty()) {
            if (CurlUtils.isCurlString(contents)) {
                initialValue = contents;
            }
        }

        final CurlRequest[] curlRequestTmp = {null};

        String curlStr = Messages.showMultilineInputDialog(
                project, null, NlsBundle.message("import.from.curl"), initialValue, HttpIcons.FILE,
                new InputValidator() {
                    @Override
                    public boolean checkInput(String str) {
                        return true;
                    }

                    @Override
                    public boolean canClose(String str) {
                        if (str == null) return false;

                        try {
                            curlRequestTmp[0] = new CurlParser(str).parseToCurlRequest();
                        } catch (Exception ex) {
                            NotifyUtil.notifyError(project, ex.toString());
                            return false;
                        }

                        return true;
                    }
                }
        );

        if (curlStr == null) return;

        CurlRequest curlRequest = curlRequestTmp[0];

        String httpStr = toHttpRequest(curlRequest, curlStr);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        Document document = FileDocumentManager.getInstance().getDocument(editor.getVirtualFile());

        getApplication().runWriteAction(() -> {
            runWriteCommandAction(project, () -> {
                document.insertString(document.getTextLength(), httpStr);

                editor.getCaretModel().moveToOffset(document.getTextLength());

                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            });
        });
    }

    public static String toHttpRequest(CurlRequest curlRequest, String curlStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        sb.append("### curl request\n");
        sb.append("/*\n");
        sb.append(CurlUtils.createCurlStringComment(curlStr));
        sb.append("*/\n");
        sb.append(curlRequest.getHttpMethod());
        sb.append(" ");
        sb.append(curlRequest.toString());
        sb.append("\n");

        curlRequest.getHeaders().forEach((key, value) -> {
            sb.append(key).append(": ").append(value).append("\n");
        });
        sb.append("\n");

        String multipartBoundary = curlRequest.getMultipartBoundary();
        if (multipartBoundary == null) {
            String textToSend = curlRequest.getTextToSend();
            if (textToSend != null) {
                sb.append(textToSend);
                sb.append("\n\n");
            }
        } else {
            curlRequest.getFormBodyPart().forEach(formBodyPart -> {
                sb.append("--").append(multipartBoundary).append("\n");

                var bodyPart = formBodyPart.toBodyPart();
                for (var field : bodyPart.getHeader().getFields()) {
                    sb.append(field.getName()).append(": ").append(field.getBody()).append("\n");
                }

                sb.append("\n");

                var body = bodyPart.getBody();
                if (body instanceof FileBody) {
                    FileBody fileBody = (FileBody) body;
                    sb.append("< ").append(fileBody.getFile().getAbsolutePath().replace("\\", "/")).append("\n");
                } else if (body instanceof StringBody) {
                    StringBody stringBody = (StringBody) body;
                    try {
                        String text = new BufferedReader(new InputStreamReader(stringBody.getReader()))
                                .lines().collect(Collectors.joining("\n"));
                        sb.append(text).append("\n");
                    } catch (Exception ignored) {
                    }
                }
            });

            sb.append("--").append(multipartBoundary).append("--");
        }

        return sb.toString();
    }
}
