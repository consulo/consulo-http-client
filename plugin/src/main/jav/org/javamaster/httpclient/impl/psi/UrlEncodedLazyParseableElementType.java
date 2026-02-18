package org.javamaster.httpclient.impl.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.impl.parser.GeneratedParserUtilBase;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.version.LanguageVersionUtil;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient._HttpLexer;
import org.javamaster.httpclient.impl.psi.impl.UrlEncodedLazyFileElement;
import org.javamaster.httpclient.parser.HttpAdapter;
import org.javamaster.httpclient.parser.HttpParser;
import org.javamaster.httpclient.parser.HttpParserDefinition;

/**
 * @author yudong
 */
public class UrlEncodedLazyParseableElementType extends ILazyParseableElementType {
    private static final HttpParserDefinition parserDefinition = new HttpParserDefinition();

    public UrlEncodedLazyParseableElementType(String debugName) {
        super(debugName);
    }

    @Override
    public ASTNode parseContents(ASTNode chameleon) {
        UrlEncodedLazyFileElement urlEncodedLazyFileElement = (UrlEncodedLazyFileElement) chameleon;

        HttpAdapter httpAdapter = new HttpAdapter() {
            @Override
            public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
                (((_HttpLexer) this.getFlex())).nameFlag = true;
                super.start(buffer, startOffset, endOffset, _HttpLexer.IN_QUERY);
            }
        };

        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(
            parserDefinition, httpAdapter, LanguageVersionUtil.findDefaultVersion(HttpLanguage.INSTANCE), urlEncodedLazyFileElement.getBuffer()
        );

        parseLight(psiBuilder);

        return psiBuilder.getTreeBuilt();
    }

    private void parseLight(PsiBuilder b) {
        PsiBuilder builder = b;
        IElementType t = MyHttpTypes.URL_ENCODED_FILE;
        builder = GeneratedParserUtilBase.adapt_builder_(t, builder, parserDefinition.createParser(null), null);
        PsiBuilder.Marker m = GeneratedParserUtilBase.enter_section_(builder, 0, GeneratedParserUtilBase._COLLAPSE_, null);
        boolean r = parseRoot(builder);
        GeneratedParserUtilBase.exit_section_(builder, 0, m, t, r, true, GeneratedParserUtilBase.TRUE_CONDITION);
    }

    private boolean parseRoot(PsiBuilder b) {
        return HttpParser.query(b, 1);
    }
}
