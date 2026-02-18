package org.javamaster.httpclient.impl.curl;

import com.google.common.net.HttpHeaders;
import consulo.project.Project;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.impl.curl.data.CurlAuthData;
import org.javamaster.httpclient.impl.curl.data.CurlFormData;
import org.javamaster.httpclient.impl.curl.exception.CurlParseException;
import org.javamaster.httpclient.impl.curl.support.CurlDataOptionFactory;
import org.javamaster.httpclient.impl.curl.support.CurlFormBodyPart;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;
import org.javamaster.httpclient.impl.curl.support.CurlTokenizer;
import org.javamaster.httpclient.impl.dashboard.HttpProcessHandler;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.CurlUtils;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.psi.HttpRequestBlock;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

public class CurlParser {
    private static final String BOUNDARY = "WebAppBoundary";

    private final String curl;
    private String myContentType;
    private String myAuthSchemes;

    public CurlParser(String curl) {
        this.curl = curl;
    }

    public CurlRequest parseToCurlRequest() {
        if (!CurlUtils.isCurlString(curl)) {
            throw CurlParseException.newNotCurlException(curl);
        }

        final List<String> tokens = CurlTokenizer.splitInCurlTokens(curl);

        final CurlRequest curlRequest = new CurlRequest();
        int i = 1;

        while (i < tokens.size()) {
            final String currentToken = tokens.get(i);
            if (currentToken.equals("\\")) {
                ++i;
            } else {
                String nextToken = null;
                if (i + 1 < tokens.size()) {
                    nextToken = tokens.get(i + 1);
                }

                i += chooseCategory(curlRequest, currentToken, deleteBackslashes(nextToken));
            }
        }

        if (curlRequest.getUrlBase() == null) {
            throw CurlParseException.newNoUrlException();
        }

        if (myContentType != null) {
            addContentTypeHeaderToRequest(curlRequest);
        }

        return curlRequest;
    }

    private int chooseCategory(CurlRequest request, String currentToken, String nextToken) {
        int shift = 1;

        if (CurlUtils.isLongOption(currentToken)) {
            shift = addLongOption(request, currentToken.substring(2), nextToken);
        } else if (CurlUtils.isShortOption(currentToken)) {
            shift = addShortOption(request, currentToken.substring(1), nextToken);
        } else {
            addURL(request, currentToken);
        }

        return shift;
    }

    private int addShortOption(CurlRequest request, String option, String nextToken) {
        if (CurlUtils.isAlwaysSetShortOption(option)) {
            return 1;
        }

        if (!CurlUtils.isKnownShortOption(option)) {
            throw CurlParseException.newNotSupportedOptionException(option);
        }

        String nextTokenTmp = nextToken;
        final boolean withoutSpace;

        if (option.length() > 1) {
            withoutSpace = true;
            nextTokenTmp = option.substring(1);
        } else {
            withoutSpace = false;
        }

        if (nextTokenTmp == null) {
            throw CurlParseException.newNoRequiredOptionDataException(option);
        }

        char optionChar = option.charAt(0);
        switch (optionChar) {
            case 'F':
                addFormDataToRequest(request, nextTokenTmp);
                break;
            case 'H':
                addHeaderToRequest(request, nextTokenTmp);
                break;
            case 'X':
                addHttpMethodToRequest(request, nextTokenTmp);
                break;
            case 'd':
                addDataToRequest("data", request, nextTokenTmp);
                break;
            case 'u':
                addAuthorizationDataToRequest(request, nextTokenTmp);
                break;
        }

        return withoutSpace ? 1 : 2;
    }

    private int addLongOption(CurlRequest request, String option, String nextToken) {
        if (CurlUtils.isAlwaysSetLongOption(option)) {
            return 1;
        }

        if (isAuthSchemeOption(request, option)) {
            return 1;
        }

        if (!CurlUtils.isKnownLongOption(option)) {
            throw CurlParseException.newNotSupportedOptionException(option);
        }

        if (nextToken == null) {
            throw CurlParseException.newNoRequiredOptionDataException(option);
        }

        if (option.startsWith("data")) {
            addDataToRequest(option, request, nextToken);
            return 2;
        }

        switch (option) {
            case "url":
                addURL(request, nextToken);
                break;
            case "request":
                addHttpMethodToRequest(request, nextToken);
                break;
            case "header":
                addHeaderToRequest(request, nextToken);
                break;
            case "user":
                addAuthorizationDataToRequest(request, nextToken);
                break;
            case "form":
                addFormDataToRequest(request, nextToken);
                break;
        }

        return 2;
    }

    private void addHeaderToRequest(CurlRequest request, String header) {
        final CurlRequest.KeyValuePair keyValueHeaderPair = getKeyValueForHeader(header);

        if (keyValueHeaderPair.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
            if (myContentType != null) {
                myContentType = updateContentTypeIfNeeded(request, keyValueHeaderPair.getValue(), myContentType);
            } else {
                myContentType = keyValueHeaderPair.getValue();
            }

            request.setMultipartBoundary(detectBoundary(header));
        } else {
            request.getHeaders().add(getKeyValueForHeader(header));
        }
    }

    private String detectBoundary(String header) {
        final String[] split = header.split(";");

        if (split.length <= 1) {
            return null;
        }

        for (String part : split) {
            final String[] keyValue = part.split("=");
            if (keyValue.length <= 1) {
                continue;
            }

            if (!keyValue[0].trim().equals("boundary")) {
                continue;
            }

            return keyValue[1].trim();
        }

        return null;
    }

    private void addDataToRequest(String optionName, CurlRequest request, String data) {
        request.setHttpMethod(HttpRequestEnum.POST.name());

        final Object curlDataOption = CurlDataOptionFactory.getCurlDataOption(optionName, data);

        if (curlDataOption != null) {
            if (curlDataOption instanceof Consumer) {
                @SuppressWarnings("unchecked")
                Consumer<CurlRequest> consumer = (Consumer<CurlRequest>) curlDataOption;
                consumer.accept(request);
            }
        }

        if (myContentType == null) {
            final String header = HttpHeaders.CONTENT_TYPE + ": " + ContentType.APPLICATION_FORM_URLENCODED.getMimeType();

            addHeaderToRequest(request, header);
        }
    }

    private void addAuthorizationDataToRequest(CurlRequest request, String authData) {
        final AuthScope authScope = new AuthScope(
            AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM, myAuthSchemes == null ? "Basic" : myAuthSchemes
        );

        String password = "";
        final int colonPosition = authData.indexOf(':');
        final String username;

        if (colonPosition < 0) {
            username = authData;
        } else {
            username = authData.substring(0, colonPosition);
            password = authData.substring(colonPosition + 1);
        }

        request.setAuthData(new CurlAuthData(authScope, new UsernamePasswordCredentials(username, password)));
    }

    private boolean isAuthSchemeOption(CurlRequest request, String option) {
        switch (option) {
            case "basic":
                myAuthSchemes = "Basic";
                break;
            case "digest":
                myAuthSchemes = "Digest";
                break;
            case "ntlm":
                myAuthSchemes = "NTLM";
                break;
            case "negotiate":
                myAuthSchemes = "Negotiate";
                break;
            default:
                return false;
        }

        if (request.getAuthData() == null) {
            return true;
        }

        final AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM, myAuthSchemes);

        request.setAuthData(new CurlAuthData(authScope, request.getAuthData().getAuthCredentials()));

        return true;
    }

    private void addFormDataToRequest(CurlRequest request, String formData) {
        final CurlFormData curlFormData = new CurlFormData(formData);

        final String fieldName = curlFormData.getName();
        final CurlFormBodyPart curlFormBodyPart;

        if (curlFormData.hasFileContent()) {
            final File file = curlFormData.getFile();
            if (file == null) {
                throw CurlParseException.newInvalidFormDataException(formData);
            }

            final String filename = file.getName();

            curlFormBodyPart = CurlFormBodyPart.create(fieldName, filename, file, curlFormData.getFormContentType());
            curlFormBodyPart.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                "form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"");
        } else {
            curlFormBodyPart = CurlFormBodyPart.create(fieldName, curlFormData.getContent(), curlFormData.getFormContentType());
            curlFormBodyPart.addHeader(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"" + fieldName + "\"");
        }

        for (CurlRequest.KeyValuePair header : curlFormData.getHeaders()) {
            curlFormBodyPart.addHeader(header.getKey(), header.getValue());
        }

        request.getFormBodyPart().add(curlFormBodyPart);

        request.setHttpMethod(HttpRequestEnum.POST.name());
        request.setFileUpload(true);

        if (request.getMultipartBoundary() == null) {
            request.setMultipartBoundary(BOUNDARY);
        }

        if (myContentType == null) {
            final String header = HttpHeaders.CONTENT_TYPE + ": " + ContentType.MULTIPART_FORM_DATA.getMimeType();

            addHeaderToRequest(request, header);
        }
    }

    private void addContentTypeHeaderToRequest(CurlRequest request) {
        final String header = HttpHeaders.CONTENT_TYPE + ": " + myContentType;

        request.getHeaders().add(getKeyValueForHeader(header));
    }

    public static String deleteBackslashes(String data) {
        if (data == null) {
            return null;
        }
        return data.replaceAll("\\\\\\\\", "");
    }

    public static void toCurlString(HttpRequestBlock requestBlock, Project project, boolean raw, Consumer<String> consumer) {
        final HttpRequestBlock.HttpRequest request = requestBlock.getRequest();

        final HttpEditorTopForm editorTopForm = HttpEditorTopForm.getSelectedEditorTopForm(project);

        final HttpProcessHandler httpProcessHandler = new HttpProcessHandler(
            request.getMethod(),
            editorTopForm != null ? editorTopForm.getSelectedEnv() : null
        );

        httpProcessHandler.prepareJsAndConvertToCurl(raw, consumer);
    }

    private static String updateContentTypeIfNeeded(
        CurlRequest request,
        String headerValue,
        String contentType
    ) {
        String updatedContentType = contentType;

        if (request.getMultipartBoundary() != null) {
            if (contentType.equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                updatedContentType = headerValue;
            }
        } else if (contentType.equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
            updatedContentType = headerValue;
        } else {
            updatedContentType = contentType + ", " + headerValue;
        }

        return updatedContentType;
    }

    private static void addHttpMethodToRequest(CurlRequest request, String method) {
        if (!CurlUtils.isValidRequestOption(method)) {
            throw CurlParseException.newInvalidMethodException(method);
        }

        request.setHttpMethod(method);
    }

    private static void addURL(CurlRequest request, String currentToken) {
        if (request.getHttpMethod() == null) {
            addHttpMethodToRequest(request, HttpRequestEnum.GET.name());
        }

        try {
            new URI(currentToken);
            request.setUrlBase(currentToken);
            request.setUrlPath("");
        } catch (URISyntaxException e) {
            throw CurlParseException.newInvalidUrlException(currentToken, e);
        }
    }

    private static CurlRequest.KeyValuePair getKeyValueForHeader(String header) {
        final int colonPosition = header.indexOf(':');
        if (colonPosition < 0) {
            return new CurlRequest.KeyValuePair(header.trim().replaceAll(";$", ""), "");
        }

        final String name = header.substring(0, colonPosition).trim();
        if (name.isEmpty()) {
            throw CurlParseException.newInvalidHeaderException(header);
        }

        final String value = header.substring(colonPosition + 1);
        return new CurlRequest.KeyValuePair(name, value.trim());
    }
}
