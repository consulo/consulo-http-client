package org.javamaster.httpclient.psi.impl;

import com.google.common.net.HttpHeaders;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ReferenceProvidersRegistry;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.factory.HttpPsiFactory;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.utils.DubboUtilsPart;

import java.util.List;

/**
 * @author yudong
 */
public class HttpPsiImplUtil {

    public static consulo.http.HttpVersion getVersion(HttpVersion httpVersion) {
        String text = httpVersion.getText();
        if (text.contains("1.1")) {
            return consulo.http.HttpVersion.HTTP_1_1;
        }
        else {
            return consulo.http.HttpVersion.HTTP_2;
        }
    }

    public static consulo.http.HttpVersion getHttpVersion(HttpRequest request) {
        HttpVersion version = request.getVersion();
        if (version == null) {
            return consulo.http.HttpVersion.HTTP_1_1;
        }
        return getVersion(version);
    }

    public static String getName(HttpVariableName variableName) {
        return variableName.getText();
    }

    public static String getName(HttpGlobalVariableName variableName) {
        PsiElement element = HttpPsiUtils.getNextSiblingByType(variableName.getFirstChild(), HttpTypes.GLOBAL_NAME, false);
        return element != null ? element.getText() : "";
    }

    public static PsiElement setName(HttpGlobalVariableName variableName, String name) {
        HttpGlobalVariableName globalVariableName = HttpPsiFactory.createGlobalVariableName(variableName.getProject(), "@" + name + " =");
        return variableName.replace(globalVariableName);
    }

    public static PsiElement getNameIdentifier(HttpGlobalVariableName variableName) {
        PsiElement firstChild = variableName.getFirstChild();
        PsiElement element = HttpPsiUtils.getNextSiblingByType(firstChild, HttpTypes.GLOBAL_NAME, false);
        return element != null ? element : firstChild;
    }

    public static PsiReference[] getReferences(HttpGlobalVariableName variableName) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(variableName);
    }

    public static PsiReference[] getReferences(HttpQueryParameterKey element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element);
    }

    public static PsiReference[] getReferences(HttpPathAbsolute element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element);
    }

    public static boolean isBuiltin(HttpVariableName variableName) {
        return variableName.getVariableBuiltin() != null;
    }

    public static String getName(HttpHeaderField headerField) {
        return headerField.getHeaderFieldName().getText();
    }

    public static String getValue(HttpHeaderField headerField) {
        HttpHeaderFieldValue value = headerField.getHeaderFieldValue();
        return value != null ? value.getText() : null;
    }

    public static String getUrl(HttpRequestTarget httpRequestTarget) {
        return httpRequestTarget.getText();
    }

    public static HttpContentType getContentType(HttpRequest request) {
        HttpHeader header = request.getHeader();
        return getContentType(header != null ? header.getHeaderFieldList() : null);
    }

    public static HttpHeaderField getContentTypeField(HttpHeader header) {
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                return field;
            }
        }
        return null;
    }

    public static HttpHeaderField getInterfaceField(HttpHeader header) {
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(DubboUtilsPart.INTERFACE_KEY)) {
                return field;
            }
        }
        return null;
    }

    public static String getContentTypeBoundary(HttpRequest request) {
        HttpHeader header = request.getHeader();
        if (header == null) {
            return null;
        }

        HttpHeaderField first = null;
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                first = field;
                break;
            }
        }

        if (first == null) {
            return null;
        }

        HttpHeaderFieldValue headerFieldValue = first.getHeaderFieldValue();
        if (headerFieldValue == null) {
            return null;
        }

        return getHeaderFieldOption(headerFieldValue, "boundary");
    }

    public static String getContentDispositionName(HttpHeader header) {
        HttpHeaderField first = null;
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
                first = field;
                break;
            }
        }

        if (first == null) {
            return null;
        }

        HttpHeaderFieldValue headerFieldValue = first.getHeaderFieldValue();
        if (headerFieldValue == null) {
            return null;
        }

        return getHeaderFieldOption(headerFieldValue, "name");
    }

    public static String getContentDispositionFileName(HttpHeader header) {
        HttpHeaderField first = null;
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
                first = field;
                break;
            }
        }

        if (first == null) {
            return null;
        }

        HttpHeaderFieldValue headerFieldValue = first.getHeaderFieldValue();
        if (headerFieldValue == null) {
            return null;
        }

        return getHeaderFieldOption(headerFieldValue, "filename");
    }

    public static Integer getContentLength(HttpRequest request) {
        HttpHeader header = request.getHeader();
        if (header == null) {
            return null;
        }

        HttpHeaderField first = null;
        for (HttpHeaderField field : header.getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                first = field;
                break;
            }
        }

        if (first == null) {
            return null;
        }

        HttpHeaderFieldValue value = first.getHeaderFieldValue();
        if (value == null) {
            return null;
        }

        String text = value.getText();
        return Integer.parseInt(text);
    }

    public static String getHttpHost(HttpRequest request) {
        HttpRequestTarget target = request.getRequestTarget();
        if (target == null) {
            return "";
        }

        HttpHost host = target.getHost();
        if (host == null) {
            HttpPathAbsolute pathAbsolute = target.getPathAbsolute();
            return pathAbsolute != null ? pathAbsolute.getText() : "";
        }
        else {
            HttpPort port = target.getPort();
            return host.getText() + (port != null ? ":" + port.getText() : "");
        }
    }

    public static HttpContentType getContentType(List<HttpHeaderField> headerFieldList) {
        if (headerFieldList == null) {
            return null;
        }

        HttpHeaderField first = null;
        for (HttpHeaderField field : headerFieldList) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                first = field;
                break;
            }
        }

        if (first == null) {
            return null;
        }

        HttpHeaderFieldValue headerFieldValue = first.getHeaderFieldValue();
        if (headerFieldValue == null) {
            return null;
        }

        PsiElement firstChild = headerFieldValue.getFirstChild();
        if (firstChild == null) {
            return null;
        }

        String headerFieldValueText = firstChild.getText();
        String[] parts = headerFieldValueText.split(";");
        String value = parts[0];
        return new HttpContentType(value, null, List.of());
    }

    public static HttpContentType getContentType(HttpMultipartField request) {
        return getContentType(request.getHeader().getHeaderFieldList());
    }

    public static PsiReference[] getReferences(HttpDirectionValue param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static PsiReference[] getReferences(HttpRequestTarget param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static PsiReference[] getReferences(HttpVariableName param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static PsiReference[] getReferences(HttpVariableArg param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static Object getValue(HttpVariableArg param) {
        PsiElement integer = param.getInteger();
        if (integer != null) {
            return Integer.parseInt(integer.getText());
        }
        else {
            PsiElement string = param.getString();
            String text = string.getText();
            return text.substring(1, text.length() - 1);
        }
    }

    public static Object[] toArgsList(HttpVariableArgs param) {
        List<HttpVariableArg> argList = param.getVariableArgList();
        Object[] result = new Object[argList.size()];
        for (int i = 0; i < argList.size(); i++) {
            result[i] = getValue(argList.get(i));
        }
        return result;
    }

    public static PsiReference[] getReferences(HttpFilePath param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static HttpHeaderFieldValue getMultipartFieldDescription(HttpMultipartField part) {
        for (HttpHeaderField field : part.getHeader().getHeaderFieldList()) {
            if (field.getHeaderFieldName().getText().equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
                return field.getHeaderFieldValue();
            }
        }
        return null;
    }

    public static String getHeaderFieldOption(HttpHeaderFieldValue value, String optionName) {
        PsiElement child = value.getFirstChild();
        while (child != null) {
            if (isOfType(child, HttpTypes.FIELD_VALUE)) {
                String text = child.getText();
                String[] split = text.split(";");
                for (String s : split) {
                    String tmp = s.trim();
                    if (tmp.startsWith(optionName)) {
                        String[] parts = tmp.split("=");
                        return StringUtil.unquoteString(parts[1]);
                    }
                }
            }

            child = child.getNextSibling();
        }

        return null;
    }

    public static boolean isOfType(PsiElement element, IElementType type) {
        if (element == null) {
            return false;
        }

        ASTNode node = element.getNode();
        return node != null && node.getElementType() == type;
    }

    public static PsiReference[] getReferences(HttpHeaderFieldValue param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }
}
