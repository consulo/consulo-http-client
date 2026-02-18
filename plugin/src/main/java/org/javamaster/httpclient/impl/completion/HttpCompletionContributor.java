package org.javamaster.httpclient.impl.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionInitializationContext;
import consulo.language.editor.completion.CompletionType;
import consulo.util.lang.StringUtil;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.ast.TokenSet;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.impl.completion.provider.*;
import org.javamaster.httpclient.psi.*;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpCompletionContributor extends CompletionContributor {
    private final TokenSet identifierPredecessor = TokenSet.create(
        HttpTypes.IDENTIFIER,
        HttpTypes.START_VARIABLE_BRACE
    );

    public HttpCompletionContributor() {
        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                HttpHeaderFieldName.class
            ),
            new HttpHeaderFieldNamesProvider()
        );

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                HttpHeaderFieldValue.class
            ),
            new HttpHeaderFieldValuesProvider()
        );

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(HttpTypes.REQUEST_METHOD),
            new HttpMethodsProvider()
        );

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(HttpTypes.DIRECTION_NAME_PART),
            new HttpDirectionNameCompletionProvider()
        );

        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(HttpTypes.FILE_PATH_PART),
                PlatformPatterns.psiElement(HttpTypes.DIRECTION_VALUE_PART)
            ),
            new HttpFilePathCompletionProvider()
        );
    }

    @Override
    public void beforeCompletion(CompletionInitializationContext context) {
        super.beforeCompletion(context);

        PsiElement psiElement = context.getFile().findElementAt(context.getStartOffset());
        if (psiElement != null && HttpPsiUtils.isOfType(psiElement, HttpTypes.FIELD_VALUE)) {
            int startOffset = psiElement.getTextRange().getStartOffset();
            int separator = psiElement.getText().indexOf(",", context.getStartOffset() - startOffset);
            context.setReplacementOffset(separator < 0 ? psiElement.getTextRange().getEndOffset() : startOffset + separator);
        } else if (
            !isDummyIdentifierCanExtendMessageBody(psiElement, context)
            && !isBeforePossiblePreRequestHandler(context)
        ) {
            PsiElement parent = psiElement != null ? psiElement.getParent() : null;
            if (parent instanceof HttpSchema) {
                context.setReplacementOffset(getSchemeReplacementOffset(parent));
            }

            if (parent instanceof HttpHeaderFieldName || parent instanceof HttpHost) {
                context.setReplacementOffset(parent.getTextRange().getEndOffset());
            }

            if (parent instanceof HttpRequestTarget) {
                context.setReplacementOffset(parent.getTextRange().getStartOffset());
                if (context.getStartOffset() > 0) {
                    PsiElement previousElement = context.getFile().findElementAt(context.getStartOffset() - 1);
                    PsiElement previousElementParent = previousElement != null ? previousElement.getParent() : null;
                    if (previousElementParent instanceof HttpSchema) {
                        context.setReplacementOffset(getSchemeReplacementOffset(previousElementParent));
                    }
                }
            } else {
                PsiElement toReplace = getReplacedIdentifier(context, parent);
                if (toReplace != null) {
                    context.setReplacementOffset(toReplace.getTextRange().getEndOffset());
                }
            }
        } else {
            context.setDummyIdentifier("");
        }
    }

    private boolean isDummyIdentifierCanExtendMessageBody(
        PsiElement element,
        CompletionInitializationContext context
    ) {
        if (!(element instanceof PsiWhiteSpace)) {
            return false;
        }

        PsiElement prevLeaf = PsiTreeUtil.prevLeaf(element);
        if (prevLeaf != null && HttpPsiUtils.isOfType(prevLeaf, HttpTypes.MESSAGE_TEXT)) {
            var document = context.getEditor().getDocument();
            return document.getLineNumber(prevLeaf.getTextRange().getEndOffset()) != document.getLineNumber(context.getStartOffset());
        }

        return false;
    }

    private boolean isBeforePossiblePreRequestHandler(CompletionInitializationContext context) {
        HttpPreRequestHandler preHandler = PsiTreeUtil.findElementOfClassAtOffset(
            context.getFile(), context.getStartOffset(),
            HttpPreRequestHandler.class, false
        );
        if (preHandler != null && preHandler.getTextRange().getStartOffset() == context.getStartOffset()) {
            return true;
        }

        var document = context.getEditor().getDocument();
        int currentIndex = context.getStartOffset();
        CharSequence sequence = document.getCharsSequence();
        while (currentIndex < sequence.length() && !StringUtil.isLineBreak(sequence.charAt(currentIndex)) && sequence.charAt(currentIndex) != '<') {
            ++currentIndex;
        }

        if (currentIndex <= sequence.length() - 1 && sequence.charAt(currentIndex) == '<') {
            int possibleOpenBracePosition = StringUtil.skipWhitespaceForward(sequence, currentIndex + 1);
            return possibleOpenBracePosition != sequence.length() && StringUtil.startsWith(
                sequence,
                possibleOpenBracePosition,
                "{%"
            );
        } else {
            return false;
        }
    }

    private int getSchemeReplacementOffset(PsiElement scheme) {
        PsiElement possibleSeparator = scheme.getNextSibling();
        if (possibleSeparator != null && HttpPsiUtils.isOfType(
                possibleSeparator,
                HttpTypes.MESSAGE_BOUNDARY
            )
        ) {
            PsiElement possibleHost = possibleSeparator.getNextSibling();
            return (possibleHost != null && HttpPsiUtils.isOfType(
                    possibleHost,
                    HttpTypes.HOST
                )) ? possibleHost.getTextRange().getEndOffset() : possibleSeparator.getTextRange().getEndOffset();
        } else {
            return scheme.getTextRange().getEndOffset();
        }
    }

    private PsiElement getReplacedIdentifier(
        CompletionInitializationContext context,
        PsiElement parent
    ) {
        if (parent instanceof HttpVariable) {
            PsiElement toReplace = parent.getFirstChild();
            return toReplace != null ? toReplace : parent.getFirstChild();
        } else {
            if (context.getStartOffset() <= 0) return null;

            PsiElement prevElement = context.getFile().findElementAt(context.getStartOffset() - 1);

            if (prevElement != null && HttpPsiUtils.isOfTypes(prevElement, identifierPredecessor)) {
                return prevElement;
            }

            return null;
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
