package org.javamaster.httpclient.impl.copyPaste;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.action.CopyPastePreProcessor;
import consulo.codeEditor.Editor;
import consulo.codeEditor.RawText;
import consulo.project.Project;
import consulo.language.psi.PsiFile;
import org.javamaster.httpclient.impl.action.addHttp.ImportCurlAction;
import org.javamaster.httpclient.impl.curl.CurlParser;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.impl.utils.CurlUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
@ExtensionImpl
public class CurlCopyPastePreProcessor implements CopyPastePreProcessor {

    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        return null;
    }

    @NotNull
    @Override
    public String preprocessOnPaste(@NotNull Project project, @NotNull PsiFile file, 
                                     @NotNull Editor editor, @NotNull String text, @Nullable RawText rawText) {
        if (!(file instanceof HttpFile)) {
            return text;
        }

        if (!CurlUtils.isCurlString(text)) {
            return text;
        }

        try {
            CurlParser curlParser = new CurlParser(text);
            CurlRequest request = curlParser.parseToCurlRequest();
            return ImportCurlAction.toHttpRequest(request, text);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }
}
