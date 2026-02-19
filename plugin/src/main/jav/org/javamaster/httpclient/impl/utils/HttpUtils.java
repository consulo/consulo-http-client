package org.javamaster.httpclient.impl.utils;

import com.google.gson.*;
import consulo.application.util.function.Computable;
import consulo.document.util.TextRange;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;
import consulo.util.lang.Trinity;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.factory.HttpPsiFactory;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.impl.factory.JsonPsiFactory;
import org.javamaster.httpclient.impl.js.JsExecutor;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.impl.runconfig.HttpConfigurationType;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.utils.HttpUtilsPart;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HttpUtils extends HttpUtilsPart {

    public static final String WEB_BOUNDARY = "boundary";

    private static final String VARIABLE_SIGN_END = "}}";

    public static final Key<Runnable> gutterIconLoadingKey = Key.create("GUTTER_ICON_LOADING_KEY");
    public static final Key<Integer> requestFinishedKey = Key.create("REQUEST_FINISHED_KEY");

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    public static RunnerAndConfigurationSettings saveConfiguration(
        String tabName,
        Project project,
        String selectedEnv,
        HttpMethod httpMethod
    ) {
        RunManager runManager = RunManager.getInstance(project);

        RunnerAndConfigurationSettings configurationSettings = runManager.getAllSettings()
            .stream()
            .filter(it -> it.getConfiguration() instanceof HttpRunConfiguration
                && it.getConfiguration().getName().equals(tabName))
            .findFirst()
            .orElse(null);

        boolean configNotExists = configurationSettings == null;

        HttpRunConfiguration httpRunConfiguration;
        if (configNotExists) {
            configurationSettings = runManager.createConfiguration(tabName, HttpConfigurationType.class);
            httpRunConfiguration = (HttpRunConfiguration) configurationSettings.getConfiguration();
        } else {
            httpRunConfiguration = (HttpRunConfiguration) configurationSettings.getConfiguration();
        }

        configurationSettings.setActivateToolWindowBeforeRun(false);

        httpRunConfiguration.setEnv(selectedEnv != null ? selectedEnv : "");
        httpRunConfiguration.setHttpFilePath(httpMethod.getContainingFile().getVirtualFile().getPath());

        if (configNotExists) {
            runManager.addConfiguration(configurationSettings);
        }

        runManager.setSelectedConfiguration(configurationSettings);

        return configurationSettings;
    }

    public static HttpMessageBody getInjectHost(JsonStringLiteral jsonString, Project project) {
        if (!jsonString.isPropertyName()) {
            return null;
        }

        PsiElement injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(jsonString);
        if (!(injectionHost instanceof HttpMessageBody)) {
            return null;
        }

        return (HttpMessageBody) injectionHost;
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
                return UrlEncodingKt.urlEncode(list[0]) + "=" + UrlEncodingKt.urlEncode(list[1]);
            })
            .collect(Collectors.joining("&"));
    }

    public static Object convertToReqBody(
        org.javamaster.httpclient.psi.HttpRequest request,
        VariableResolver variableResolver,
        Map<String, String> paramMap
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
                paramMap
            );
        }

        HttpMultipartMessage httpMultipartMessage = body != null ? body.getMultipartMessage() : null;
        if (httpMultipartMessage != null) {
            String boundary = request.getContentTypeBoundary();
            if (boundary == null) {
                throw new IllegalArgumentException(NlsBundle.message("lack.boundary", HttpHeaders.CONTENT_TYPE));
            }

            return constructMultipartBody(boundary, httpMultipartMessage, variableResolver, paramMap);
        }

        return null;
    }

    private static boolean isTxtContentType(HttpHeader header) {
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
        ContentType contentType,
        Map<String, String> paramMap
    ) {
        if (requestMessagesGroup == null) {
            return null;
        }

        boolean shouldEncode = contentType == ContentType.APPLICATION_FORM_URLENCODED
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
            } else {
                reqStr += CR_LF;
            }

            String str = VirtualFileUtils.readNewestContent(file);

            if (shouldEncode) {
                str = encodeQueryParam(str);
            }

            reqStr += variableResolver.resolve(str);

            return reqStr;
        } else {
            byte[] byteArray = VirtualFileUtils.readNewestBytes(file);

            String size = Formats.formatFileSize(byteArray.length);

            String desc = NlsBundle.message("binary.body.desc", size, file.getAbsolutePath());

            return new Pair<>(byteArray, desc);
        }
    }

    private static List<Pair<byte[], String>> constructMultipartBody(
        String boundary,
        HttpMultipartMessage httpMultipartMessage,
        VariableResolver variableResolver,
        Map<String, String> paramMap
    ) {
        List<Pair<byte[], String>> byteArrays = new ArrayList<>();

        httpMultipartMessage.getMultipartFieldList().forEach(it -> {
            String lineBoundary = "--" + boundary + CR_LF;
            byteArrays.add(new Pair<>(lineBoundary.getBytes(), lineBoundary));

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
                byteArrays.add(new Pair<>(headerLine.getBytes(StandardCharsets.UTF_8), headerLine));
            });

            byteArrays.add(new Pair<>(CR_LF.getBytes(StandardCharsets.UTF_8), CR_LF));

            Object content = handleOrdinaryContent(
                it.getRequestMessagesGroup(),
                variableResolver,
                it.getHeader(),
                it.getContentType(),
                paramMap
            );

            if (content instanceof String) {
                String tmpContent = (String) content + CR_LF;

                byteArrays.add(new Pair<>(tmpContent.getBytes(StandardCharsets.UTF_8), tmpContent));
            } else if (content instanceof Pair) {
                @SuppressWarnings("unchecked")
                Pair<byte[], String> pair = (Pair<byte[], String>) content;

                byte[] bytes = pair.first;
                String desc = pair.second;

                byte[] crlfBytes = CR_LF.getBytes(StandardCharsets.UTF_8);
                byte[] combined = new byte[bytes.length + crlfBytes.length];
                System.arraycopy(bytes, 0, combined, 0, bytes.length);
                System.arraycopy(crlfBytes, 0, combined, bytes.length, crlfBytes.length);

                byteArrays.add(new Pair<>(combined, desc + CR_LF));
            }
        });

        String endBoundary = "--" + boundary + "--";
        byteArrays.add(new Pair<>(endBoundary.getBytes(StandardCharsets.UTF_8), endBoundary));

        return byteArrays;
    }

    public static String handleOrdinaryContentCurl(
        HttpRequestMessagesGroup requestMessagesGroup,
        VariableResolver variableResolver,
        HttpHeader header,
        boolean raw
    ) {
        String reqStr = "";

        HttpMessageBody messageBody = requestMessagesGroup.getMessageBody();
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.getText());
        }

        HttpInputFile inputFile = requestMessagesGroup.getInputFile();
        String filePath = inputFile != null && inputFile.getFilePath() != null
            ? inputFile.getFilePath().getText()
            : null;

        if (filePath == null) {
            return raw ? reqStr + CR_LF : reqStr.replace("\n", "\n    ").replace("'", "'\\''");
        }

        String path = constructFilePath(filePath, variableResolver.getHttpFileParentPath());

        File file = new File(path);

        if (!isTxtContentType(header)) {
            return "";
        }

        reqStr += CR_LF;

        String str = VirtualFileUtils.readNewestContent(file);

        reqStr += variableResolver.resolve(str);

        return raw ? reqStr + CR_LF : reqStr.replace("\n", "\n    ").replace("'", "'\\''");
    }

    public static List<String> constructMultipartBodyCurl(
        HttpMultipartMessage httpMultipartMessage,
        VariableResolver variableResolver,
        String boundary,
        boolean raw
    ) {
        List<String> list = new ArrayList<>();

        httpMultipartMessage.getMultipartFieldList().forEach(it -> {
            HttpRequestMessagesGroup requestMessagesGroup = it.getRequestMessagesGroup();
            HttpHeader header = it.getHeader();

            if (raw) {
                list.add("--" + boundary + CR_LF);

                header.getHeaderFieldList().forEach(innerIt -> {
                    list.add(innerIt.getName() + ": " + innerIt.getValue() + CR_LF);
                });

                list.add(CR_LF);
            }

            HttpMessageBody messageBody = requestMessagesGroup.getMessageBody();
            if (messageBody != null) {
                String content = variableResolver.resolve(messageBody.getText());

                if (raw) {
                    list.add(content + CR_LF);
                } else {
                    String contentTypeText = header.getContentTypeField() != null
                        && header.getContentTypeField().getHeaderFieldValue() != null
                        ? header.getContentTypeField().getHeaderFieldValue().getText()
                        : "";
                    list.add("    -F \"" + header.getContentDispositionName() + "=" + content + ";type=" + contentTypeText + "\"");
                }
            }

            HttpInputFile inputFile = requestMessagesGroup.getInputFile();
            String filePath = inputFile != null && inputFile.getFilePath() != null
                ? inputFile.getFilePath().getText()
                : null;

            if (filePath != null) {
                String path = constructFilePath(filePath, variableResolver.getHttpFileParentPath());

                File file = new File(path);

                String content = "@" + file.getAbsolutePath().replace("\\", "/");

                if (raw) {
                    list.add("< " + file.getAbsolutePath() + CR_LF);
                } else {
                    String contentTypeText = header.getContentTypeField() != null
                        && header.getContentTypeField().getHeaderFieldValue() != null
                        ? header.getContentTypeField().getHeaderFieldValue().getText()
                        : "";
                    list.add("    -F \"" + header.getContentDispositionName() + "=" + content + ";filename="
                        + header.getContentDispositionFileName() + ";type=" + contentTypeText + "\"");
                }
            }
        });

        if (raw) {
            list.add("--" + boundary + "--");
        }

        return list;
    }

    public static List<String> convertToResHeaderDescList(HttpResponse<byte[]> response) {
        List<String> headerDescList = new ArrayList<>();
        java.net.http.HttpHeaders headers = response.headers();
        headers.map().forEach((key, values) -> {
            values.forEach(value -> {
                headerDescList.add(key + ": " + value + CR_LF);
            });
        });

        headerDescList.add(CR_LF);

        return headerDescList;
    }

    public static Trinity<SimpleTypeEnum, byte[], String> convertToResPair(HttpResponse<byte[]> response) {
        byte[] resBody = response.body();
        java.net.http.HttpHeaders resHeaders = response.headers();
        String contentType = resHeaders.firstValue(HttpHeaders.CONTENT_TYPE)
            .orElse(ContentType.TEXT_PLAIN.getMimeType());

        SimpleTypeEnum simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType);

        if (simpleTypeEnum == SimpleTypeEnum.JSON) {
            String jsonStr = new String(resBody, StandardCharsets.UTF_8);

            try {
                JsonElement jsonElement = gson.fromJson(jsonStr, JsonElement.class);
                String jsonStrPretty = gson.toJson(jsonElement);

                return new Trinity<>(simpleTypeEnum, jsonStrPretty.getBytes(StandardCharsets.UTF_8), contentType);
            } catch (JsonSyntaxException e) {
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

    public static PsiElement resolveFileGlobalVariable(String variableName, PsiFile httpFile) {
        Collection<HttpGlobalVariable> globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable.class);

        return globalVariables.stream()
            .map(it -> {
                HttpGlobalVariableName globalVariableName = it.getGlobalVariableName();
                if (globalVariableName.getName().equals(variableName)) {
                    return globalVariableName;
                } else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
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
                    } else {
                        String urlText = it.getDirectionValue() != null ? it.getDirectionValue().getText() : null;
                        if (urlText == null) {
                            return null;
                        }
                        try {
                            return new PreJsFile(it, new URL(urlText));
                        } catch (Exception e) {
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

    public static List<HttpScriptBody> getAllPostJsScripts(PsiFile httpFile) {
        Collection<HttpResponseHandler> handlers = PsiTreeUtil.findChildrenOfType(httpFile, HttpResponseHandler.class);

        return handlers.stream()
            .map(HttpUtils::getJsScript)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
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
    public static Pair<String, TextRange> getSearchTxtInfo(HttpRequestTarget requestTarget, String httpFileParentPath) {
        Project project = requestTarget.getProject();

        String url = requestTarget.getText();

        int start;
        int bracketIdx = url.indexOf(VARIABLE_SIGN_END);
        if (bracketIdx != -1) {
            start = bracketIdx + 2;
        } else {
            EnvFileService envFileService = EnvFileService.getService(project);
            String selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

            String contextPath = envFileService.getEnvValue("contextPath", selectedEnv, httpFileParentPath);
            String contextPathTrim = envFileService.getEnvValue("contextPathTrim", selectedEnv, httpFileParentPath);

            int tmpIdx;
            URI uri;
            try {
                uri = new URI(url);
                if (contextPath != null) {
                    tmpIdx = url.indexOf(contextPath);
                } else if (contextPathTrim != null) {
                    tmpIdx = url.indexOf(contextPathTrim) + contextPathTrim.length();
                } else {
                    tmpIdx = url.indexOf(uri.getPath());
                }
            } catch (Exception e) {
                return null;
            }
            start = tmpIdx;
        }

        if (start == -1) {
            return null;
        }

        int idx = url.lastIndexOf("?");
        int end = idx == -1 ? url.length() : idx;

        if (end < start) {
            return null;
        }

        TextRange textRange = new TextRange(start, end);
        String searchTxt = url.substring(start, end);
        return new Pair<>(searchTxt, textRange);
    }

    public static boolean isHistoryFile(VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.getNameWithoutExtension().endsWith("history");
    }

    public static LinkedList<String> collectJsonPropertyNameLevels(JsonStringLiteral jsonString) {
        LinkedList<String> beanFieldLevels = new LinkedList<>();

        JsonProperty jsonProperty = PsiTreeUtil.getParentOfType(jsonString, JsonProperty.class);
        while (jsonProperty != null) {
            String propertyName = getJsonPropertyName(jsonProperty);
            beanFieldLevels.push(propertyName);
            jsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty.class);
        }

        return beanFieldLevels;
    }

    private static String getJsonPropertyName(JsonProperty jsonProperty) {
        JsonElement nameElement = jsonProperty.getNameElement();
        String name = nameElement.getText();
        return name.substring(1, name.length() - 1);
    }

    public static PsiParameter resolveTargetParam(PsiMethod psiMethod) {
        PsiMethod[] superPsiMethods = psiMethod.findSuperMethods(false);
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        PsiParameter psiParameter = null;

        for (int index = 0; index < psiParameters.length; index++) {
            PsiParameter psiParam = psiParameters[index];
            boolean hasAnno = psiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME);
            if (hasAnno) {
                psiParameter = psiParam;
                break;
            }

            for (PsiMethod superPsiMethod : superPsiMethods) {
                PsiParameter superPsiParam = superPsiMethod.getParameterList().getParameters()[index];
                hasAnno = superPsiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME);
                if (hasAnno) {
                    psiParameter = psiParam;
                    break;
                }
            }
        }

        return psiParameter;
    }

    public static PsiField resolveTargetField(
        PsiClass paramPsiCls,
        LinkedList<String> jsonPropertyNameLevels,
        PsiType[] classGenericParameters
    ) {
        PsiField psiField = null;

        try {
            PsiClass fieldTypeCls;
            String propertyName = jsonPropertyNameLevels.pop();

            boolean isCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection");
            if (isCollection) {
                if (classGenericParameters.length == 0) {
                    return null;
                }

                // Get the generic parameter type
                fieldTypeCls = PsiUtils.resolvePsiType(classGenericParameters[0]);
                if (fieldTypeCls == null) {
                    return null;
                }
            } else {
                fieldTypeCls = paramPsiCls;
            }

            while (true) {
                psiField = fieldTypeCls.findFieldByName(propertyName, true);
                if (psiField == null) {
                    return null;
                }

                if (!(psiField.getType() instanceof PsiClassType)) {
                    return psiField;
                }

                PsiClassType psiType = (PsiClassType) psiField.getType();

                PsiType[] parameters = psiType.getParameters();
                if (parameters.length > 0) {
                    // Get the generic parameter type
                    fieldTypeCls = PsiUtils.resolvePsiType(parameters[0]);
                    if (fieldTypeCls == null) {
                        return null;
                    }
                } else {
                    PsiClass psiFieldTypeCls = PsiUtils.resolvePsiType(psiType);
                    if (psiFieldTypeCls == null) {
                        return null;
                    }

                    if (psiFieldTypeCls instanceof PsiTypeParameter && classGenericParameters.length > 0) {
                        // The parameter itself is a generic type, such as T, and the first one is taken directly
                        PsiClassType genericActualType = (PsiClassType) classGenericParameters[0];
                        if (genericActualType.getParameters().length > 0) {
                            PsiClass psiFieldGenericTypeCls = PsiUtils.resolvePsiType(genericActualType.getParameters()[0]);
                            if (psiFieldGenericTypeCls == null) {
                                return null;
                            }
                            fieldTypeCls = psiFieldGenericTypeCls;
                        } else {
                            fieldTypeCls = PsiUtils.resolvePsiType(genericActualType);
                            if (fieldTypeCls == null) {
                                return null;
                            }
                        }
                    } else {
                        fieldTypeCls = psiFieldTypeCls;
                    }
                }

                propertyName = jsonPropertyNameLevels.pop();
            }
        } catch (NoSuchElementException e) {
            // End of iteration
        }

        return psiField;
    }

    public static String convertToJsString(String str) {
        return "`" + str.replace("\\", "\\\\").replace("`", "\\`") + "`";
    }

    public static Object convertReqBody(Object reqBody) {
        if (reqBody == null) {
            return "null";
        }

        if (reqBody instanceof String) {
            return convertToJsString((String) reqBody);
        }

        if (reqBody instanceof Pair) {
            @SuppressWarnings("unchecked")
            Pair<byte[], String> pair = (Pair<byte[], String>) reqBody;

            return pair.first;
        }

        if (reqBody instanceof List) {
            @SuppressWarnings("unchecked")
            List<Pair<byte[], String>> list = (List<Pair<byte[], String>>) reqBody;

            return list.stream()
                .map(it -> it.first)
                .reduce((a, b) -> {
                    byte[] combined = new byte[a.length + b.length];
                    System.arraycopy(a, 0, combined, 0, a.length);
                    System.arraycopy(b, 0, combined, a.length, b.length);
                    return combined;
                })
                .orElse(new byte[0]);
        }

        throw new IllegalArgumentException(NlsBundle.message("reqBody.unknown", reqBody.getClass().toString()));
    }


    public static PsiElement createGlobalVariableAndInsert(String variableName, String variableValue, Project project) {
        var textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (textEditor == null) {
            return null;
        }

        HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());

        PsiElement newGlobalVariable = HttpPsiFactory.createGlobalVariable(variableName, variableValue, project);

        List<HttpDirectionComment> directionComments = httpFile.getDirectionComments();
        HttpGlobalHandler globalHandler = httpFile.getGlobalHandler();

        PsiElement elementCopy;
        if (!directionComments.isEmpty()) {
            elementCopy = httpFile.addAfter(newGlobalVariable, directionComments.get(directionComments.size() - 1).getNextSibling());
        } else if (globalHandler != null) {
            elementCopy = httpFile.addAfter(newGlobalVariable, globalHandler);
        } else {
            elementCopy = httpFile.addBefore(newGlobalVariable, httpFile.getFirstChild());
        }

        PsiElement whitespace = newGlobalVariable.getNextSibling();
        elementCopy.add(whitespace);

        PsiElement cr = whitespace.getNextSibling();
        if (cr != null) {
            elementCopy.add(cr);
        }

        return elementCopy;
    }

    public static boolean modifyFileGlobalVariable(
        String key,
        String newKey,
        String newValue,
        boolean add,
        Project project
    ) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<Boolean>) () -> {
            if (add) {
                PsiElement variable = createGlobalVariableAndInsert(newKey, newValue, project);

                return variable != null;
            } else {
                var textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (textEditor == null) {
                    return false;
                }

                HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());
                Collection<HttpGlobalVariable> children = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable.class);

                HttpGlobalVariable globalVariable = children.stream()
                    .filter(it -> it.getGlobalVariableName().getName().equals(key))
                    .findFirst()
                    .orElse(null);

                if (globalVariable == null) {
                    return false;
                }

                if (!key.equals(newKey)) {
                    RenameProcessor renameProcessor = new RenameProcessor(
                        project, globalVariable, newKey,
                        GlobalSearchScope.projectScope(project), false, true
                    );
                    renameProcessor.run();
                }

                PsiElement newGlobalVariable = HttpPsiFactory.createGlobalVariable(newKey, newValue, project);

                globalVariable.replace(newGlobalVariable);
            }

            return true;
        });
    }

    public static void modifyJsVariable(String newKey, String newValue) {
        JsExecutor.setGlobalVariable(newKey, newValue);
    }

    public static boolean modifyEnvVariable(
        String key,
        String newKey,
        String newValue,
        boolean add,
        Project project
    ) {
        var triple = HttpEditorTopForm.getTriple(project);
        if (triple == null) {
            return false;
        }

        String selectedEnv = triple.first;
        String httpFileParentPath = triple.second.getParent().getPath();

        if (add) {
            JsonProperty jsonProperty = EnvFileService.getEnvJsonProperty(selectedEnv, httpFileParentPath, project);
            if (jsonProperty == null) {
                return false;
            }

            JsonValue jsonValue = jsonProperty.getValue();
            if (!(jsonValue instanceof JsonObject)) {
                return false;
            }

            JsonObject jsonObject = (JsonObject) jsonValue;

            WriteCommandAction.runWriteCommandAction(project, () -> {
                JsonProperty newProperty = JsonPsiFactory.createStringProperty(project, newKey, newValue);
                PsiElement newComma = HttpPsiUtils.getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false);
                List<JsonProperty> propertyList = jsonObject.getPropertyList();

                if (propertyList.isEmpty()) {
                    jsonObject.addAfter(newProperty, jsonObject.getFirstChild());
                } else {
                    PsiElement psiElement = jsonObject.addAfter(newComma, propertyList.get(propertyList.size() - 1));
                    jsonObject.addAfter(newProperty, psiElement);
                }
            });
        } else {
            JsonElement jsonLiteral = EnvFileService.getEnvEleLiteral(key, selectedEnv, httpFileParentPath, project);
            if (jsonLiteral == null) {
                return false;
            }

            PsiElement jsonProperty = jsonLiteral.getParent();

            if (!key.equals(newKey)) {
                RenameProcessor renameProcessor = new RenameProcessor(
                    project, jsonProperty, newKey,
                    GlobalSearchScope.projectScope(project), false, true
                );
                renameProcessor.run();
            }

            WriteCommandAction.runWriteCommandAction(project, () -> {
                JsonProperty newProperty;
                if (jsonLiteral instanceof JsonNumberLiteral) {
                    newProperty = JsonPsiFactory.createNumberProperty(project, newKey, newValue);
                } else if (jsonLiteral instanceof JsonBooleanLiteral) {
                    newProperty = JsonPsiFactory.createBoolProperty(project, newKey, newValue);
                } else {
                    newProperty = JsonPsiFactory.createStringProperty(project, newKey, newValue);
                }

                jsonProperty.replace(newProperty);
            });
        }

        return true;
    }
}
