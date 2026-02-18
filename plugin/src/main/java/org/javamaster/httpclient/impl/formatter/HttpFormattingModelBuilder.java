package org.javamaster.httpclient.impl.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.codeStyle.FormattingContext;
import consulo.language.codeStyle.FormattingModel;
import consulo.language.codeStyle.FormattingModelBuilder;
import consulo.language.codeStyle.FormattingModelProvider;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiFile;
import consulo.language.codeStyle.CodeStyleSettings;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpFormattingModelBuilder implements FormattingModelBuilder {

    @NotNull
    @Override
    public FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        PsiFile containingFile = formattingContext.getContainingFile();

        ASTNode fileNode = containingFile.getNode();

        CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();

        HttpRequestFileBlock fileBlock = new HttpRequestFileBlock(fileNode, codeStyleSettings);

        return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, fileBlock, codeStyleSettings);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
