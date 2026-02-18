package org.javamaster.httpclient.impl.typeHandler;

import consulo.language.editor.action.TypedHandlerDelegate;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.codeEditor.util.EditorModificationUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.PsiUtilCore;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpHeader;
import org.javamaster.httpclient.psi.HttpHeaderField;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HttpTypedHandler extends TypedHandlerDelegate {

    @NotNull
    @Override
    public Result beforeCharTyped(
            char c,
            @NotNull Project project,
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @NotNull FileType fileType
    ) {
        VirtualFile virtualFile = editor.getVirtualFile();
        if (virtualFile == null) {
            return Result.CONTINUE;
        }

        PsiFile psiFile = PsiUtil.getPsiFile(project, virtualFile);

        if (!(psiFile instanceof HttpFile) || (c != '%' && c != '{')) {
            return Result.CONTINUE;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAtOffset = file.findElementAt(offset - 1);
        if (elementAtOffset == null) {
            return Result.CONTINUE;
        }

        Document document = editor.getDocument();
        char charBefore = getCharAt(document, offset - 1);
        char charAfter = getCharAt(document, offset);

        if (charBefore == '{') {
            if (c == '%' && charAfter != '%') {
                return addBrace(project, editor, document, "%\n    \n%}", 6);
            }

            if (c == '{' && charAfter != '}') {
                return addBrace(project, editor, document, "{}}", 1);
            }
        } else if (c == '{' && charAfter != '{' && couldCompleteToMessageBody(elementAtOffset, document, offset)) {
            return addBrace(project, editor, document, "{}", 1);
        }

        return Result.CONTINUE;
    }

    private Result addBrace(Project project, Editor editor, Document document, String s, int caretShift) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        if (documentManager != null) {
            EditorModificationUtil.insertStringAtCaret(editor, s, true, caretShift);
            documentManager.commitDocument(document);
            return Result.STOP;
        } else {
            return Result.CONTINUE;
        }
    }

    private char getCharAt(Document document, int offset) {
        return (offset < document.getTextLength() && offset >= 0) ?
                document.getCharsSequence().charAt(offset) : '\u0000';
    }

    private boolean couldCompleteToMessageBody(PsiElement element, Document document, int offset) {
        if (!(element instanceof PsiWhiteSpace)) {
            return false;
        }

        PsiElement sibling = element.getPrevSibling();
        if (sibling instanceof HttpRequestBlock) {
            HttpRequestBlock requestBlock = (HttpRequestBlock) sibling;
            HttpRequest request = requestBlock.getRequest();
            if (request == null) {
                return false;
            }

            HttpHeader header = request.getHeader();
            if (header == null) {
                return false;
            }

            List<HttpHeaderField> headerFieldList = header.getHeaderFieldList();
            if (headerFieldList == null || headerFieldList.isEmpty()) {
                return false;
            }
        } else if (!(sibling instanceof HttpHeaderField)) {
            return false;
        }

        int prevOffset = offset - 1;
        int countOfLineBreaks = 0;
        while (StringUtil.isWhiteSpace(getCharAt(document, prevOffset))) {
            if (StringUtil.isLineBreak(getCharAt(document, prevOffset))) {
                ++countOfLineBreaks;
            }
            --prevOffset;
        }

        return countOfLineBreaks > 1;
    }
}
