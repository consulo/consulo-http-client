package org.javamaster.httpclient.impl.completion.support;

import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.document.Document;
import consulo.codeEditor.Editor;

/**
 * @author yudong
 */
public class SlashEndInsertHandler implements InsertHandler<LookupElement> {
    public static final SlashEndInsertHandler INSTANCE = new SlashEndInsertHandler();

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        Document document = editor.getDocument();

        context.commitDocument();

        int offset = context.getTailOffset() + 2;

        document.insertString(offset, "/");

        editor.getCaretModel().moveToOffset(offset + 1);
    }
}
