package consulo.httpClient.execution.runner;

import consulo.application.ReadAction;
import consulo.http.HttpRequestBuilder;
import consulo.http.HttpRequestBuilderFactory;
import consulo.http.HttpVersion;
import consulo.httpClient.execution.HttpFileContentReader;
import consulo.httpClient.execution.HttpRequestSupport;
import consulo.httpClient.execution.env.HttpEnvironment;
import org.javamaster.httpclient.impl.js.JsExecutor;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.Trinity;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.*;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javamaster.httpclient.utils.HttpUtilsPart.CR_LF;

/**
 * Runs one request of a .http file: variables, handler scripts, sending, response description and saving.
 * <p>
 * Blocking, must be called from a background thread. PSI is accessed inside read actions only.
 * Used by the plugin run configurations and by the command line client.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
public final class HttpRequestRunner {
    private final Project myProject;
    private final HttpRequestBuilderFactory myRequestBuilderFactory;
    private final HttpFileContentReader myContentReader;

    private Platform myPlatform = Platform.current();
    private long myDefaultConnectTimeoutSeconds = HttpUtilsPart.CONNECT_TIMEOUT;
    private long myDefaultReadTimeoutSeconds = HttpUtilsPart.READ_TIMEOUT;

    /**
     * @param requestBuilderFactory platform http stack; the IDE passes its service, the command line client
     *                              one with its own proxy and certificate settings
     */
    public HttpRequestRunner(Project project, HttpRequestBuilderFactory requestBuilderFactory, HttpFileContentReader contentReader) {
        myProject = project;
        myRequestBuilderFactory = requestBuilderFactory;
        myContentReader = contentReader;
    }

    /**
     * Platform the requests are sent from: the local machine by default, or a remote one (for example a remote agent),
     * served by a {@link consulo.http.internal.PlatformHttpRequestSender}.
     */
    public HttpRequestRunner setPlatform(Platform platform) {
        myPlatform = platform;
        return this;
    }

    /**
     * Timeouts for requests without {@code # @connectTimeout} / {@code # @readTimeout} directions.
     */
    public HttpRequestRunner setDefaultTimeouts(long connectTimeoutSeconds, long readTimeoutSeconds) {
        myDefaultConnectTimeoutSeconds = connectTimeoutSeconds;
        myDefaultReadTimeoutSeconds = readTimeoutSeconds;
        return this;
    }

    /**
     * Everything taken from PSI before sending.
     */
    private record PreparedRequest(LocalizeValue tabName,
                                   HttpRequestEnum methodType,
                                   String parentPath,
                                   HttpVersion version,
                                   Map<String, String> paramMap,
                                   JsExecutor jsExecutor,
                                   VariableResolver variableResolver,
                                   HttpReqInfo reqInfo,
                                   List<HttpScriptBody> jsListBeforeReq,
                                   @Nullable HttpScriptBody jsAfterReq,
                                   LinkedMultiValueMap<String, String> reqHeaderMap,
                                   String rawUrl,
                                   @Nullable String outputFilePath) {
    }

    public HttpRequestRunResult run(HttpMethod httpMethod, @Nullable String selectedEnv, HttpEnvironment environment) {
        LocalizeValue tabName = ReadAction.compute(() -> HttpUtilsPart.getTabName(httpMethod));
        String methodName = ReadAction.compute(httpMethod::getText);

        List<String> httpReqDescList = new ArrayList<>();
        PreparedRequest prepared = null;
        String url = null;
        try {
            prepared = ReadAction.compute(() -> prepare(httpMethod, tabName, environment));
            PreparedRequest request = prepared;

            List<String> beforeJsResList = ReadAction.compute(() -> {
                request.jsExecutor().initJsRequestObj(
                    request.reqInfo(),
                    request.methodType(),
                    request.reqHeaderMap(),
                    selectedEnv,
                    request.variableResolver().getFileScopeVariableMap()
                );
                request.jsExecutor().initRequestUrl(request.rawUrl(), text -> ReadAction.compute(() -> request.variableResolver().resolve(text)));
                return request.jsExecutor().evalJsBeforeRequest(request.reqInfo().getPreJsFiles(), request.jsListBeforeReq());
            });
            httpReqDescList.addAll(beforeJsResList);

            url = ReadAction.compute(() -> request.variableResolver().resolve(request.rawUrl()));
            if (request.paramMap().containsKey(ParamEnum.AUTO_ENCODING.getParam())) {
                url = HttpRequestSupport.encodeUrl(url);
            }

            LinkedMultiValueMap<String, String> headers = ReadAction.compute(() -> {
                LinkedMultiValueMap<String, String> map = HttpRequestSupport.resolveReqHeaderMapAgain(request.reqHeaderMap(),
                    request.variableResolver());
                map.putAll(request.jsExecutor().getHeaderMap());
                return map;
            });

            // body was resolved before pre-request scripts ran, fill variables they set
            Object reqBody = request.reqInfo().getReqBody();
            if (reqBody instanceof String text) {
                reqBody = ReadAction.compute(() -> request.variableResolver().resolve(text));
            }

            return send(request, url, headers, reqBody, httpReqDescList);
        }
        catch (Throwable e) {
            HttpInfo httpInfo = new HttpInfo(httpReqDescList, new ArrayList<>(), null, null, e);
            Map<String, String> paramMap = prepared == null ? Map.of() : prepared.paramMap();
            return new HttpRequestRunResult(tabName, methodName, url, httpInfo, null, e, 0, List.of(), paramMap, null);
        }
    }

    private PreparedRequest prepare(HttpMethod httpMethod, LocalizeValue tabName, HttpEnvironment environment) throws Exception {
        HttpFile httpFile = (HttpFile) httpMethod.getContainingFile();
        String parentPath = httpFile.getVirtualFile().getParent().getPath();

        HttpRequestTarget requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget.class);
        HttpRequest request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest.class);
        HttpRequestBlock requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock.class);
        if (requestTarget == null || request == null || requestBlock == null) {
            throw new IllegalArgumentException(HttpClientLocalize.methodUnsupported(httpMethod.getText()).get());
        }

        HttpRequestEnum methodType = HttpRequestEnum.getInstance(httpMethod.getText());
        HttpResponseHandler responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler.class);

        List<PreJsFile> preJsFiles = HttpRequestSupport.getPreJsFiles(httpFile, false);
        for (PreJsFile preJsFile : preJsFiles) {
            File file = preJsFile.getFile();
            if (file != null) {
                preJsFile.setContent(myContentReader.readText(file));
            }
        }

        Map<String, String> paramMap = HttpRequestSupport.getReqDirectionCommentParamMap(requestBlock);

        org.javamaster.httpclient.psi.HttpVersion httpVersion = request.getVersion();
        HttpVersion version = httpVersion != null ? httpVersion.getVersion() : HttpVersion.HTTP_1_1;

        JsExecutor jsExecutor = JsExecutor.create(myProject, httpFile, tabName);
        VariableResolver variableResolver = new VariableResolver(jsExecutor, httpFile, environment, myProject);

        Object reqBody = HttpRequestSupport.convertToReqBody(request, variableResolver, paramMap, myContentReader);
        String environmentJson = HttpUtilsPart.gson.toJson(environment.asMap());
        HttpReqInfo reqInfo = new HttpReqInfo(reqBody, environmentJson, preJsFiles);

        HttpHeader header = request.getHeader();
        LinkedMultiValueMap<String, String> reqHeaderMap =
            HttpRequestSupport.convertToReqHeaderMap(header != null ? header.getHeaderFieldList() : null, variableResolver);

        String outputFilePath = null;
        HttpOutputFile outputFile = PsiTreeUtil.getChildOfType(request, HttpOutputFile.class);
        if (outputFile != null && outputFile.getFilePath() != null) {
            outputFilePath = outputFile.getFilePath().getText();
        }

        return new PreparedRequest(
            tabName,
            methodType,
            parentPath,
            version,
            paramMap,
            jsExecutor,
            variableResolver,
            reqInfo,
            HttpRequestSupport.getAllPreJsScripts(httpFile, requestBlock),
            HttpRequestSupport.getJsScript(responseHandler),
            reqHeaderMap,
            requestTarget.getUrl(),
            outputFilePath
        );
    }

    private HttpRequestRunResult send(PreparedRequest request,
                                      String url,
                                      LinkedMultiValueMap<String, String> headers,
                                      @Nullable Object reqBody,
                                      List<String> httpReqDescList) throws Exception {
        HttpRequestEnum methodType = request.methodType();
        Map<String, String> paramMap = request.paramMap();

        byte[] body = toBodyBytes(reqBody);
        if (methodType == HttpRequestEnum.GET) {
            body = new byte[0];
        }

        httpReqDescList.add(methodType.name() + " " + url + " " + HttpUtilsPart.getVersionDesc(request.version()) + CR_LF);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                httpReqDescList.add(entry.getKey() + ": " + value + CR_LF);
            }
        }
        httpReqDescList.add("Content-Length: " + body.length + CR_LF);
        httpReqDescList.add(0, "// " + HttpClientLocalize.reqSize(StringUtil.formatFileSize(body.length)).get() + CR_LF);
        httpReqDescList.add(CR_LF);
        httpReqDescList.addAll(HttpUtilsPart.getReqBodyDesc(reqBody));

        long connectTimeout = parseSeconds(paramMap, ParamEnum.CONNECT_TIMEOUT_NAME, myDefaultConnectTimeoutSeconds);
        long readTimeout = parseSeconds(paramMap, ParamEnum.READ_TIMEOUT_NAME, myDefaultReadTimeoutSeconds);

        long start = System.currentTimeMillis();
        HttpResponse response = sendRequest(methodType, url, request.version(), headers, body, connectTimeout, readTimeout);
        long costMillis = System.currentTimeMillis() - start;

        String size = StringUtil.formatFileSize(response.body().length);
        Trinity<SimpleTypeEnum, byte[], String> resTriple = HttpRequestSupport.convertToResPair(response);

        List<String> httpResDescList = new ArrayList<>();
        httpResDescList.add("// " + HttpClientLocalize.resDesc(response.statusCode(), costMillis, size).get() + CR_LF);

        String evalJsRes = ReadAction.compute(() -> request.jsExecutor().evalJsAfterRequest(
            request.jsAfterReq(),
            resTriple,
            response.statusCode(),
            response.headers()
        ));
        if (evalJsRes != null && !evalJsRes.isEmpty()) {
            httpResDescList.add("/*" + CR_LF + HttpClientLocalize.postJsExecutedResult().get() + ":" + CR_LF);
            httpResDescList.add(evalJsRes + CR_LF);
            httpResDescList.add("*/" + CR_LF);
        }

        httpResDescList.add("### " + request.tabName() + CR_LF);
        httpResDescList.add(methodType.name() + " " + response.uri() + " " + HttpUtilsPart.getVersionDesc(response.version()) + CR_LF);
        httpResDescList.addAll(HttpRequestSupport.convertToResHeaderDescList(response));

        if (resTriple.getFirst().isBinary()) {
            httpResDescList.add(HttpClientLocalize.resBinaryData(size).get());
        }
        else {
            httpResDescList.add(new String(resTriple.getSecond(), StandardCharsets.UTF_8));
        }

        String savedFile = null;
        if (request.outputFilePath() != null) {
            String path = ReadAction.compute(() -> request.variableResolver().resolve(request.outputFilePath()));
            try {
                savedFile = saveToFile(HttpUtilsPart.constructFilePath(path, request.parentPath()), resTriple.getSecond());
                httpResDescList.add(0, "// " + HttpClientLocalize.saveToFile().get() + ": " + savedFile + CR_LF);
            }
            catch (Exception e) {
                httpResDescList.add(0, "// " + HttpClientLocalize.saveFailed().get() + ": " + e + CR_LF);
            }
        }

        HttpInfo httpInfo = new HttpInfo(httpReqDescList, httpResDescList, resTriple.getFirst(), resTriple.getSecond(), null,
            resTriple.getThird());

        return new HttpRequestRunResult(
            request.tabName(),
            methodType.name(),
            url,
            httpInfo,
            response,
            null,
            costMillis,
            request.jsExecutor().getTestResults(),
            paramMap,
            savedFile
        );
    }

    private HttpResponse sendRequest(HttpRequestEnum methodType,
                                     String url,
                                     HttpVersion version,
                                     LinkedMultiValueMap<String, String> headers,
                                     byte[] body,
                                     long connectTimeoutSeconds,
                                     long readTimeoutSeconds) throws Exception {
        HttpRequestBuilder builder = myRequestBuilderFactory.newBuilder(myPlatform, url, methodType.getHttpMethod());
        builder.allowErrorCodes(true);
        builder.version(version);
        builder.connectTimeout((int) connectTimeoutSeconds * 1000);
        builder.readTimeout((int) readTimeoutSeconds * 1000);

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }

        if (body.length > 0) {
            builder.body(body);
        }

        return builder.connect(it -> new HttpResponse(
            it.statusCode(),
            it.statusMessage(),
            it.responseHeaders(),
            it.getURL(),
            it.version(),
            it.readBytes(null)
        ));
    }

    private static String saveToFile(String path, byte[] bytes) throws Exception {
        Path file = Path.of(path).toAbsolutePath().normalize();
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, bytes);
        return file.toString();
    }

    private static long parseSeconds(Map<String, String> paramMap, ParamEnum param, long defaultValue) {
        String value = paramMap.get(param.getParam());
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(value.trim());
    }

    /**
     * Request body as built by {@link HttpRequestSupport#convertToReqBody}: a string, a binary file
     * ({@code Pair<byte[], description>}) or multipart parts ({@code List<Pair<byte[], description>>}).
     */
    @SuppressWarnings("unchecked")
    public static byte[] toBodyBytes(@Nullable Object reqBody) {
        if (reqBody == null) {
            return new byte[0];
        }
        if (reqBody instanceof String text) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        if (reqBody instanceof Pair<?, ?> pair) {
            return (byte[]) pair.getFirst();
        }
        if (reqBody instanceof List<?> list) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (Object part : list) {
                out.writeBytes(((Pair<byte[], String>) part).getFirst());
            }
            return out.toByteArray();
        }
        throw new IllegalArgumentException(HttpClientLocalize.reqbodyUnknown(reqBody.getClass().toString()).get());
    }
}
