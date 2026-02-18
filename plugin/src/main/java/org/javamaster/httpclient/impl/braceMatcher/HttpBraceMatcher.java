package org.javamaster.httpclient.impl.braceMatcher;

import consulo.language.editor.highlight.PairedBraceMatcherAdapter;
import consulo.language.BracePair;
import consulo.language.PairedBraceMatcher;
import consulo.language.psi.PsiFile;
import consulo.language.ast.IElementType;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpBraceMatcher extends PairedBraceMatcherAdapter {

    public HttpBraceMatcher() {
        super(new MyPairedBraceMatcher(), HttpLanguage.INSTANCE);
    }

    private static class MyPairedBraceMatcher implements PairedBraceMatcher {
        private static final BracePair[] PAIRS = new BracePair[]{
                new BracePair(HttpTypes.START_VARIABLE_BRACE, HttpTypes.END_VARIABLE_BRACE, true),
                new BracePair(HttpTypes.OUT_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true),
                new BracePair(HttpTypes.GLOBAL_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true),
                new BracePair(HttpTypes.LEFT_BRACKET, HttpTypes.RIGHT_BRACKET, true),
                new BracePair(HttpTypes.IN_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true)
        };

        @NotNull
        @Override
        public BracePair[] getPairs() {
            return PAIRS;
        }

        @Override
        public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
            return true;
        }

        @Override
        public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
            return openingBraceOffset;
        }
    }
}
