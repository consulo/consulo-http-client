package consulo.httpClient.execution;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.Trinity;
import com.google.common.net.HttpHeaders;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpResponse;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.javamaster.httpclient.utils.HttpUtilsPart.CR_LF;
import static org.javamaster.httpclient.utils.HttpUtilsPart.constructFilePath;
import static org.javamaster.httpclient.utils.HttpUtilsPart.gson;

/**
 * Converts request PSI into data for sending: headers, body, handler scripts, directions.
 * Moved from the plugin HttpUtils, so the plugin and the command line client share it.
 *
 * @author yudong
 */
public final class HttpRequestSupport {
    private HttpRequestSupport() {
    }

    public static LinkedMultiValueMap<String, String> convertToReqHeaderMap(
        List<HttpHeaderField> headerFields,
        VariableResolver variableResolver
    ) {
        if (headerFields == null || headerFields.isEmpty()) {
            return new LinkedMultiValueMap<>();
        }

        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        headerFields.forEach(it -> {
            String headerName = it.getHeaderFieldName().getText();
            String headerValue = it.getHeaderFieldValue() != null ? it.getHeaderFieldValue().getText() : "";
            map.add(headerName, variableResolver.resolve(headerValue));
        });

        return map;
    }


    public static LinkedMultiValueMap<String, String> resolveReqHeaderMapAgain(
        LinkedMultiValueMap<String, String> reqHeaderMap,
        VariableResolver variableResolver
    ) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        reqHeaderMap.entrySet().forEach(entry -> {
            String headerName = entry.getKey();
            List<String> values = entry.getValue();

            values.forEach(value -> map.add(headerName, variableResolver.resolve(value)));
        });

        return map;
    }


    public static String encodeUrl(String url) {
        String[] split = url.split("\\?", 2);
        if (split.length == 1) {
            return url;
        }

        return split[0] + "?" + encodeQueryParam(split[1]);
    }


    private static String encodeQueryParam(String queryParam) {
        String[] split = queryParam.split("&");

        return Arrays.stream(split)
            .map(it -> {
                String[] list = it.split("=", 2);
                return URLEncoder.encode(list[0], StandardCharsets.UTF_8) + "=" + URLEncoder.encode(list[1], StandardCharsets.UTF_8);
            })
            .collect(Collectors.joining("&"));
    }


    public static Object convertToReqBody(
        org.javamaster.httpclient.psi.HttpRequest request,
        VariableResolver variableResolver,
        Map<String, String> paramMap,
        HttpFileContentReader contentReader
    ) {
        if (request.getContentLength() != null) {
            throw new IllegalArgumentException(NlsBundle.message("content.length.error"));
        }

        HttpBody body = request.getBody();

        HttpRequestMessagesGroup requestMessagesGroup = body != null ? body.getRequestMessagesGroup() : null;
        if (requestMessagesGroup != null) {
            return handleOrdinaryContent(
                requestMessagesGroup,
                variableResolver,
                request.getHeader(),
                request.getContentType(),
                paramMap,
                contentReader
            );
        }

        HttpMultipartMessage httpMultipartMessage = body != null ? body.getMultipartMessage() : null;
        if (httpMultipartMessage != null) {
            String boundary = request.getContentTypeBoundary();
            if (boundary == null) {
                throw new IllegalArgumentException(NlsBundle.message("lack.boundary", HttpHeaders.CONTENT_TYPE));
            }

            return constructMultipartBody(boundary, httpMultipartMessage, variableResolver, paramMap, contentReader);
        }

        return null;
    }


    public static boolean isTxtContentType(HttpHeader header) {
        if (header == null) {
            return true;
        }

        HttpHeaderField headerField = header.getContentTypeField();
        if (headerField == null) {
            return true;
        }

        HttpHeaderFieldValue headerFieldValue = headerField.getHeaderFieldValue();
        if (headerFieldValue == null) {
            return true;
        }

        return SimpleTypeEnum.isTextContentType(headerFieldValue.getText());
    }


    private static Object handleOrdinaryContent(
        HttpRequestMessagesGroup requestMessagesGroup,
        VariableResolver variableResolver,
        HttpHeader header,
        HttpContentType contentType,
        Map<String, String> paramMap,
        HttpFileContentReader contentReader
    ) {
        if (requestMessagesGroup == null) {
            return null;
        }

        boolean shouldEncode = contentType != null && "application/x-www-form-urlencoded".equals(contentType.mimeType())
            && paramMap.containsKey(ParamEnum.AUTO_ENCODING.getParam());

        String reqStr = null;

        HttpMessageBody messageBody = requestMessagesGroup.getMessageBody();
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.getText());

            if (shouldEncode) {
                reqStr = encodeQueryParam(reqStr);
            }
        }

        HttpInputFile inputFile = requestMessagesGroup.getInputFile();
        String filePath = inputFile != null && inputFile.getFilePath() != null
            ? inputFile.getFilePath().getText()
            : null;

        if (filePath == null) {
            return reqStr;
        }

        String path = constructFilePath(filePath, variableResolver.getHttpFileParentPath());

        File file = new File(path);

        if (isTxtContentType(header)) {
            if (reqStr == null) {
                reqStr = "";
            }
            else {
                reqStr += CR_LF;
            }

            String str = null;
            try {
                str = contentReader.readText(file);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (shouldEncode) {
                str = encodeQueryParam(str);
            }

            reqStr += variableResolver.resolve(str);

            return reqStr;
        }
        else {
            byte[] byteArray = new byte[0];
            try {
                byteArray = contentReader.readBytes(file);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            String size = StringUtil.formatFileSize(byteArray.length);

            LocalizeValue desc = HttpClientLocalize.binaryBodyDesc(size, file.getAbsolutePath());

            return Pair.create(byteArray, desc.get());
        }
    }


    private static List<Pair<byte[], String>> constructMultipartBody(
        String boundary,
        HttpMultipartMessage httpMultipartMessage,
        VariableResolver variableResolver,
        Map<String, String> paramMap,
        HttpFileContentReader contentReader
    ) {
        List<Pair<byte[], String>> byteArrays = new ArrayList<>();

        httpMultipartMessage.getMultipartFieldList().forEach(it -> {
            String lineBoundary = "--" + boundary + CR_LF;
            byteArrays.add(Pair.create(lineBoundary.getBytes(), lineBoundary));

            HttpHeader header = it.getHeader();

            header.getHeaderFieldList().forEach(innerIt -> {
                String headerName = innerIt.getHeaderFieldName().getText();
                String headerValue = innerIt.getHeaderFieldValue() != null
                    ? innerIt.getHeaderFieldValue().getText()
                    : null;

                String value = (headerValue == null || headerValue.isEmpty())
                    ? ""
                    : variableResolver.resolve(headerValue);

                String headerLine = headerName + ": " + value + CR_LF;
                byteArrays.add(Pair.create(headerLine.getBytes(StandardCharsets.UTF_8), headerLine));
            });

            byteArrays.add(Pair.create(CR_LF.getBytes(StandardCharsets.UTF_8), CR_LF));

            Object content = handleOrdinaryContent(
                it.getRequestMessagesGroup(),
                variableResolver,
                it.getHeader(),
                it.getContentType(),
                paramMap,
                contentReader
            );

            if (content instanceof String) {
                String tmpContent = (String) content + CR_LF;

                byteArrays.add(Pair.create(tmpContent.getBytes(StandardCharsets.UTF_8), tmpContent));
            }
            else if (content instanceof Pair) {
                @SuppressWarnings("unchecked")
                Pair<byte[], String> pair = (Pair<byte[], String>) content;

                byte[] bytes = pair.first;
                String desc = pair.second;

                byte[] crlfBytes = CR_LF.getBytes(StandardCharsets.UTF_8);
                byte[] combined = new byte[bytes.length + crlfBytes.length];
                System.arraycopy(bytes, 0, combined, 0, bytes.length);
                System.arraycopy(crlfBytes, 0, combined, bytes.length, crlfBytes.length);

                byteArrays.add(Pair.create(combined, desc + CR_LF));
            }
        });

        String endBoundary = "--" + boundary + "--";
        byteArrays.add(Pair.create(endBoundary.getBytes(StandardCharsets.UTF_8), endBoundary));

        return byteArrays;
    }


    public static List<String> convertToResHeaderDescList(org.javamaster.httpclient.model.HttpResponse response) {
        List<String> headerDescList = new ArrayList<>();
        Map<String, List<String>> headers = response.headers();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                headerDescList.add(entry.getKey() + ": " + value + CR_LF);
            }
        }

        headerDescList.add(CR_LF);

        return headerDescList;
    }


    public static Trinity<SimpleTypeEnum, byte[], String> convertToResPair(HttpResponse response) {
        byte[] resBody = response.body();
        Map<String, List<String>> resHeaders = response.headers();

        String contentType = ContainerUtil.getFirstItem(resHeaders.get("Content-Type"));
        if (contentType == null) {
            contentType = "text/plain";
        }

        SimpleTypeEnum simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType);

        if (simpleTypeEnum == SimpleTypeEnum.JSON) {
            String jsonStr = new String(resBody, StandardCharsets.UTF_8);

            try {
                JsonElement jsonElement = gson.fromJson(jsonStr, JsonElement.class);
                String jsonStrPretty = gson.toJson(jsonElement);

                return new Trinity<>(simpleTypeEnum, jsonStrPretty.getBytes(StandardCharsets.UTF_8), contentType);
            }
            catch (JsonSyntaxException e) {
                return new Trinity<>(simpleTypeEnum, resBody, contentType);
            }
        }

        return new Trinity<>(simpleTypeEnum, resBody, contentType);
    }


    public static HttpScriptBody getJsScript(HttpResponseHandler httpResponseHandler) {
        if (httpResponseHandler == null) {
            return null;
        }

        return httpResponseHandler.getResponseScript().getScriptBody();
    }


    public static List<PreJsFile> getPreJsFiles(HttpFile httpFile, boolean excludeRequire) {
        List<HttpDirectionComment> directionComments = httpFile.getDirectionComments();

        String parentPath = httpFile.getVirtualFile().getParent().getPath();

        return directionComments.stream()
            .map(it -> {
                boolean isRequire = it.getDirectionName() != null
                    && ParamEnum.REQUIRE.getParam().equals(it.getDirectionName().getText());

                if (isRequire) {
                    if (excludeRequire) {
                        return null;
                    }
                    else {
                        String urlText = it.getDirectionValue() != null ? it.getDirectionValue().getText() : null;
                        if (urlText == null) {
                            return null;
                        }
                        try {
                            return new PreJsFile(it, new URL(urlText));
                        }
                        catch (Exception e) {
                            return null;
                        }
                    }
                }

                String path = getDirectionPath(it, parentPath);
                if (path == null) {
                    return null;
                }

                PreJsFile preJsFile = new PreJsFile(it, null);
                preJsFile.setFile(new File(path));

                return preJsFile;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    private static PsiDirectory resolveDirOfVariable(HttpVariable variable) {
        if (variable == null || variable.getVariableName() == null) {
            return null;
        }

        PsiReference[] references = variable.getVariableName().getReferences();
        if (references.length == 0) {
            return null;
        }

        PsiElement psiElement = references[0].resolve();
        if (!(psiElement instanceof PsiDirectory)) {
            return null;
        }

        return (PsiDirectory) psiElement;
    }


    public static String resolvePathOfVariable(HttpVariable variable) {
        PsiDirectory psiElement = resolveDirOfVariable(variable);
        if (psiElement == null) {
            return null;
        }

        return psiElement.getVirtualFile().getPath();
    }


    public static String getDirectionPath(HttpDirectionComment directionComment, String parentPath) {
        HttpDirectionValue directionValue = directionComment.getDirectionValue();
        if (directionValue == null
            || !ParamEnum.isFilePathParam(directionComment.getDirectionName() != null
            ? directionComment.getDirectionName().getText()
            : null)) {
            return null;
        }

        String path = "";

        String resolvedPath = resolvePathOfVariable(directionValue.getVariable());
        if (resolvedPath != null) {
            path += resolvedPath;
        }

        path += directionValue.getDirectionValueContent() != null
            ? directionValue.getDirectionValueContent().getText()
            : "";

        if (!path.toLowerCase().endsWith("js")) {
            return null;
        }

        return constructFilePath(path, parentPath);
    }


    public static List<HttpScriptBody> getAllPreJsScripts(PsiFile httpFile, HttpRequestBlock httpRequestBlock) {
        List<HttpScriptBody> scripts = new ArrayList<>();

        HttpScriptBody globalScript = getGlobalJsScript(httpFile);
        if (globalScript != null) {
            scripts.add(globalScript);
        }

        HttpScriptBody preJsScript = getPreJsScript(httpRequestBlock);
        if (preJsScript != null) {
            scripts.add(preJsScript);
        }

        return scripts;
    }


    private static HttpScriptBody getGlobalJsScript(PsiFile httpFile) {
        HttpGlobalHandler globalHandler = PsiTreeUtil.getChildOfType(httpFile, HttpGlobalHandler.class);
        if (globalHandler == null) {
            return null;
        }
        return globalHandler.getGlobalScript().getScriptBody();
    }


    private static HttpScriptBody getPreJsScript(HttpRequestBlock httpRequestBlock) {
        HttpPreRequestHandler preRequestHandler = httpRequestBlock.getPreRequestHandler();
        if (preRequestHandler == null) {
            return null;
        }
        return preRequestHandler.getPreRequestScript().getScriptBody();
    }


    public static Map<String, String> getReqDirectionCommentParamMap(HttpRequestBlock httpRequestBlock) {
        Map<String, String> map = new HashMap<>();

        httpRequestBlock.getDirectionCommentList().forEach(it -> {
            String name = it.getDirectionName() != null ? it.getDirectionName().getText() : null;
            if (name != null) {
                map.put(name, it.getDirectionValue() != null ? it.getDirectionValue().getText() : "");
            }
        });

        return map;
    }
}
