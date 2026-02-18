package org.javamaster.httpclient.impl.completion.support;

import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiDocumentManager;

/**
 * @author yudong
 */
public class HttpSuffixInsertHandler implements InsertHandler<LookupElement> {
    public static final HttpSuffixInsertHandler FIELD_SEPARATOR = new HttpSuffixInsertHandler(": ");

    private final String mySuffix;
    private final String myShortSuffix;

    public HttpSuffixInsertHandler(String suffix) {
        this.mySuffix = suffix;
        this.myShortSuffix = suffix.trim();
    }

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        Project project = context.getProject();
        Editor editor = context.getEditor();
        Document document = editor.getDocument();

        // When LookupElementBuilder Set the caseSensitive property to true, For some reason,
        // it becomes lowercase after completion. Here, we force correction
        String text = item.getLookupString();
        document.replaceString(context.getStartOffset(), context.getTailOffset(), text);

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        documentManager.commitDocument(editor.getDocument());

        int offset = StringUtil.skipWhitespaceForward(document.getCharsSequence(), editor.getCaretModel().getOffset());
        if (document.getTextLength() == offset || !isEqualsToSuffix(document, offset)) {
            EditorModificationUtilEx.insertStringAtCaret(editor, mySuffix);
            documentManager.commitDocument(editor.getDocument());
        }

        editor.getCaretModel().moveToOffset(offset + mySuffix.length());
    }

    private boolean isEqualsToSuffix(Document document, int offset) {
        int endOffset = offset + myShortSuffix.length() - 1;
        return document.getTextLength() > endOffset && StringUtil.equals(
            this.myShortSuffix,
            document.getCharsSequence().subSequence(offset, endOffset + 1).toString()
        );
    }

    @Override
    public String toString() {
        return this.mySuffix;
    }
}
