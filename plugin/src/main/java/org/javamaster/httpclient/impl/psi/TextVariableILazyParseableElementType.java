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
import org.javamaster.httpclient.parser.HttpAdapter;
import org.javamaster.httpclient.parser.HttpParser;
import org.javamaster.httpclient.parser.HttpParserDefinition;
import org.javamaster.httpclient.psi.HttpTypes;

/**
 * @author yudong
 */
public class TextVariableILazyParseableElementType extends ILazyParseableElementType {
    private static final HttpParserDefinition parserDefinition = new HttpParserDefinition();

    public TextVariableILazyParseableElementType(String debugName) {
        super(debugName);
    }

    @Override
    public ASTNode parseContents(ASTNode chameleon) {
        TextVariableLazyFileElement textVariableLazyFileElement = (TextVariableLazyFileElement) chameleon;

        HttpAdapter httpAdapter = new HttpAdapter() {
            @Override
            public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
                super.start(buffer, startOffset, endOffset, _HttpLexer.IN_JSON_VALUE);
            }
        };

        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(
            parserDefinition, httpAdapter, LanguageVersionUtil.findDefaultVersion(HttpLanguage.INSTANCE), textVariableLazyFileElement.getBuffer()
        );

        parseLight(psiBuilder);

        return psiBuilder.getTreeBuilt();
    }

    private void parseLight(PsiBuilder b) {
        IElementType t = HttpTypes.MY_JSON_VALUE;
        PsiBuilder builder = b;
        builder = GeneratedParserUtilBase.adapt_builder_(t, builder, parserDefinition.createParser(null), null);
        PsiBuilder.Marker m = GeneratedParserUtilBase.enter_section_(builder, 0, GeneratedParserUtilBase._COLLAPSE_, null);
        boolean r = parseRoot(builder);
        GeneratedParserUtilBase.exit_section_(builder, 0, m, t, r, true, GeneratedParserUtilBase.TRUE_CONDITION);
    }

    private boolean parseRoot(PsiBuilder b) {
        return HttpParser.myJsonValue(b, 1);
    }
}
