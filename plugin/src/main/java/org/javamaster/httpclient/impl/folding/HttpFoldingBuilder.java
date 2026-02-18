package org.javamaster.httpclient.impl.folding;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.editor.folding.FoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.javamaster.httpclient.impl.action.dashboard.view.FoldHeadersAction;
import org.javamaster.httpclient.psi.HttpHeader;
import org.javamaster.httpclient.psi.HttpMultipartField;
import org.javamaster.httpclient.psi.HttpOutputFile;
import org.javamaster.httpclient.psi.HttpTypes;
import org.javamaster.httpclient.psi.impl.HttpPsiImplUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ExtensionImpl
public class HttpFoldingBuilder implements FoldingBuilder, DumbAware {

    @RequiredReadAction
    @Nonnull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@Nonnull ASTNode node, @Nonnull Document document) {
        List<FoldingDescriptor> descriptors = collectDescriptors(node);
        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    @RequiredReadAction
    @Override
    public String getPlaceholderText(ASTNode node) {
        var type = node.getElementType();
        if (type == HttpTypes.REQUEST) {
            var httpMethodNode = node.findChildByType(HttpTypes.METHOD);
            var httpRequestTargetNode = node.findChildByType(HttpTypes.REQUEST_TARGET);
            if (httpMethodNode != null) {
                var methodType = httpMethodNode.getText();
                return methodType + (httpRequestTargetNode != null ? " " + httpRequestTargetNode.getText() : "");
            }
        }
        else if (type == HttpTypes.MULTIPART_FIELD) {
            var messagesGroupNode = node.findChildByType(HttpTypes.REQUEST_MESSAGES_GROUP);
            var contentDispositionName = getContentDispositionName(node);
            if (contentDispositionName != null) {
                if (messagesGroupNode != null) {
                    return contentDispositionName + ": " + messagesGroupNode.getText();
                }
                return contentDispositionName;
            }
            if (messagesGroupNode != null) {
                return messagesGroupNode.getText();
            }
            if (node.getFirstChildNode() != null) {
                return node.getFirstChildNode().getText();
            }
        }
        else if (type == HttpTypes.RESPONSE_HANDLER || type == HttpTypes.GLOBAL_HANDLER) {
            return "{% ... %}";
        }
        else if (type == HttpTypes.HEADER) {
            var contentTypeField = ((HttpHeader) node.getPsi()).getContentTypeField();
            if (contentTypeField != null) {
                return "(Headers)..." + contentTypeField.getText() + "...";
            }
            return "(Headers)...";
        }
        else if (type == HttpTypes.OUTPUT_FILE) {
            var filePath = ((HttpOutputFile) node.getPsi()).getFilePath();
            if (filePath != null) {
                var text = filePath.getFilePathContent() != null ? filePath.getFilePathContent().getText() : null;
                if (filePath.getVariable() == null && text != null && text.length() > 32) {
                    return Paths.get(text).getFileName().toString();
                }
                return text != null ? text : "...";
            }
        }
        else if (type == HttpTypes.BLOCK_COMMENT) {
            return "/* ... */";
        }
        return "...";
    }

    @RequiredReadAction
    @Override
    public boolean isCollapsedByDefault(@Nonnull ASTNode node) {
        boolean foldHeader = getFoldHeaderFlag(node);
        var type = node.getElementType();
        return (foldHeader && type == HttpTypes.HEADER) || type == HttpTypes.OUTPUT_FILE || type == HttpTypes.BLOCK_COMMENT;
    }

    private boolean getFoldHeaderFlag(ASTNode node) {
        PsiElement psi = node.getPsi();
        if (psi == null) {
            return false;
        }
        PsiFile psiFile = psi.getContainingFile();
        if (psiFile == null) {
            return false;
        }
        var document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
        if (document == null) {
            return true;
        }
        var foldFlag = document.getUserData(FoldHeadersAction.httpDashboardFoldHeaderKey);
        return foldFlag == null || foldFlag;
    }

    private List<FoldingDescriptor> collectDescriptors(ASTNode node) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        var blockCommentNodes = node.getChildren(TokenSet.create(HttpTypes.BLOCK_COMMENT));
        for (var it : blockCommentNodes) {
            descriptors.add(new FoldingDescriptor(it, it.getTextRange()));
        }

        var requestBlockNodes = node.getChildren(TokenSet.create(HttpTypes.REQUEST_BLOCK));

        var globalHandlerNode = node.findChildByType(HttpTypes.GLOBAL_HANDLER);
        if (globalHandlerNode != null) {
            descriptors.add(new FoldingDescriptor(globalHandlerNode, globalHandlerNode.getTextRange()));
        }

        for (var requestBlockNode : requestBlockNodes) {
            var requestNode = requestBlockNode.findChildByType(HttpTypes.REQUEST);
            if (requestNode == null) {
                continue;
            }

            descriptors.addAll(collectMultipartRequests(requestNode));
            descriptors.addAll(collectScriptPart(requestNode, requestBlockNode));

            if (requestNode.findChildByType(HttpTypes.METHOD) != null) {
                descriptors.add(new FoldingDescriptor(requestNode, requestNode.getTextRange()));
            }

            var header = requestNode.findChildByType(HttpTypes.HEADER);
            if (header != null) {
                descriptors.add(new FoldingDescriptor(header, header.getTextRange()));
            }

            var filePath = requestNode.findChildByType(HttpTypes.OUTPUT_FILE);
            if (filePath != null) {
                descriptors.add(new FoldingDescriptor(filePath, filePath.getTextRange()));
            }
        }

        return descriptors;
    }

    private List<FoldingDescriptor> collectMultipartRequests(ASTNode node) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        var bodyNode = node.findChildByType(HttpTypes.BODY);
        if (bodyNode == null) {
            return descriptors;
        }

        var multipartMessage = bodyNode.findChildByType(HttpTypes.MULTIPART_MESSAGE);
        if (multipartMessage == null) {
            return descriptors;
        }

        var multipartFields = multipartMessage.getChildren(TokenSet.create(HttpTypes.MULTIPART_FIELD));
        for (var multipartFieldNode : multipartFields) {
            var prevElement = skipCommentsAndWhitespaces(multipartFieldNode);
            int startOffset = (prevElement != null && prevElement.getElementType() == HttpTypes.MESSAGE_BOUNDARY)
                ? prevElement.getTextRange().getStartOffset()
                : multipartFieldNode.getTextRange().getStartOffset();

            descriptors.add(new FoldingDescriptor(
                multipartFieldNode,
                new TextRange(startOffset, multipartFieldNode.getTextRange().getEndOffset())
            ));
        }

        return descriptors;
    }

    private List<FoldingDescriptor> collectScriptPart(ASTNode requestNode, ASTNode requestBlockNode) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        var responseHandlerNode = requestNode.findChildByType(HttpTypes.RESPONSE_HANDLER);
        if (responseHandlerNode != null) {
            descriptors.add(new FoldingDescriptor(responseHandlerNode, responseHandlerNode.getTextRange()));
        }

        var preHandlerNode = requestBlockNode.findChildByType(HttpTypes.PRE_REQUEST_HANDLER);
        if (preHandlerNode != null) {
            descriptors.add(new FoldingDescriptor(preHandlerNode, preHandlerNode.getTextRange()));
        }

        return descriptors;
    }

    private ASTNode skipCommentsAndWhitespaces(ASTNode node) {
        var curNode = node.getTreePrev();
        while (curNode != null && curNode.getPsi() instanceof PsiWhiteSpace) {
            curNode = curNode.getTreePrev();
        }
        return curNode;
    }

    private String getContentDispositionName(ASTNode node) {
        var psiElement = node.getPsi();
        if (!(psiElement instanceof HttpMultipartField)) {
            return null;
        }
        var headerFieldValue = HttpPsiImplUtil.getMultipartFieldDescription((HttpMultipartField) psiElement);
        if (headerFieldValue == null) {
            return null;
        }
        return HttpPsiImplUtil.getHeaderFieldOption(headerFieldValue, "name");
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}