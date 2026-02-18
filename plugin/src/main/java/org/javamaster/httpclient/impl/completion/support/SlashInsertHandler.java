package org.javamaster.httpclient.impl.completion.support;

import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;

/**
 * @author yudong
 */
public class SlashInsertHandler implements InsertHandler<LookupElement> {
    public static final SlashInsertHandler INSTANCE = new SlashInsertHandler();

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        Document document = editor.getDocument();

        context.commitDocument();

        document.insertString(context.getTailOffset(), "/");
        editor.getCaretModel().moveToOffset(context.getTailOffset());
    }
}
