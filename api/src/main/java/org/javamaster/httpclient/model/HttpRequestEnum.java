package org.javamaster.httpclient.model;

import consulo.application.Application;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.http.HttpMethod;
import consulo.http.HttpRequestBuilder;
import consulo.http.HttpRequestBuilderFactory;
import consulo.http.HttpVersion;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.utils.HttpUtilsPart;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public enum HttpRequestEnum {
    GET(HttpMethod.GET, HttpIcons.GET),
    POST(HttpMethod.POST, HttpIcons.POST),
    DELETE(HttpMethod.DELETE, HttpIcons.DELETE),
    PUT(HttpMethod.PUT, HttpIcons.PUT),
    OPTIONS(HttpMethod.OPTIONS, HttpIcons.FILE),
    PATCH(HttpMethod.PATCH, HttpIcons.FILE),
    HEAD(HttpMethod.HEAD, HttpIcons.FILE),
    TRACE(HttpMethod.TRACE, HttpIcons.FILE),
    WEBSOCKET(HttpMethod.GET, HttpIcons.WS),
    //    DUBBO(HttpIcons.DUBBO),
    MOCK_SERVER(HttpMethod.GET, HttpIcons.FILE);

    private final HttpMethod myHttpMethod;
    private final Image icon;

    HttpRequestEnum(HttpMethod httpMethod, Image icon) {
        myHttpMethod = httpMethod;
        this.icon = icon;
    }

    public Image getIcon() {
        return icon;
    }

    public CompletableFuture<HttpResponse> execute(Project project,
                                                   String url,
                                                   HttpVersion version,
                                                   LinkedMultiValueMap<String, String> reqHttpHeaders,
                                                   Object reqBody,
                                                   List<String> httpReqDescList,
                                                   String tabName,
                                                   Map<String, String> paramMap) {
        try {
            Application application = Application.get();

            Pair<byte[], Long> pair = HttpUtilsPart.convertToReqBodyPublisher(reqBody);

            HttpRequestBuilderFactory requestBuilderFactory = application.getInstance(HttpRequestBuilderFactory.class);

            HttpRequestBuilder builder = requestBuilderFactory.newBuilder(url, myHttpMethod);
            builder.allowErrorCodes(true);
            builder.version(version);

            long connectTimeout = paramMap.containsKey(ParamEnum.CONNECT_TIMEOUT_NAME.getParam())
                ? Long.parseLong(paramMap.get(ParamEnum.CONNECT_TIMEOUT_NAME.getParam()))
                : HttpUtilsPart.CONNECT_TIMEOUT;

            builder.connectTimeout((int) connectTimeout * 1000);

            long readTimeout = paramMap.containsKey(ParamEnum.READ_TIMEOUT_NAME.getParam())
                ? Long.parseLong(paramMap.get(ParamEnum.READ_TIMEOUT_NAME.getParam()))
                : HttpUtilsPart.READ_TIMEOUT;
            builder.connectTimeout((int) readTimeout * 1000);

            for (Map.Entry<String, List<String>> entry : reqHttpHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.header(entry.getKey(), value);
                }
            }

            httpReqDescList.add(name() + " " + url + " " + HttpUtilsPart.getVersionDesc(version) + HttpUtilsPart.CR_LF);

            for (Map.Entry<String, List<String>> entry : reqHttpHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    httpReqDescList.add(entry.getKey() + ": " + value + HttpUtilsPart.CR_LF);
                }
            }

            long multipartLength = 0; // TODO

            long tmpLength = pair.getFirst() == null ? -1 : pair.getFirst().length;
            long contentLength = tmpLength == -1L ? multipartLength : tmpLength;
            
            if (pair.getFirst() != null) {
                builder.body(pair.getFirst());
            }

            httpReqDescList.add("Content-Length: " + contentLength + HttpUtilsPart.CR_LF);

            String size = StringUtil.formatFileSize(contentLength);
            httpReqDescList.add(0, "// " + HttpClientLocalize.reqSize(size).get() + HttpUtilsPart.CR_LF);

            httpReqDescList.add(HttpUtilsPart.CR_LF);

            List<String> descList = HttpUtilsPart.getReqBodyDesc(reqBody);
            httpReqDescList.addAll(descList);

            CompletableFuture<HttpResponse> future = new CompletableFuture<>();

            new Task.Backgroundable(project, "Calling " + url, true) {
                @Override
                public void run(@Nonnull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(true);
                    
                    try {
                        future.complete(builder.connect(it -> {
                            byte[] body = it.readBytes(null);
                            return new HttpResponse(it.statusCode(), it.statusMessage(), it.responseHeaders(), it.getURL(), it.version(), body);
                        }));
                    }
                    catch (Throwable e) {
                        future.completeExceptionally(e);
                    }
                }

                @RequiredUIAccess
                @Override
                public void onCancel() {
                    future.cancel(false);
                }
            }.queue();

            return future;
        }
        catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public static HttpRequestEnum getInstance(String methodName) {
        try {
            return HttpRequestEnum.valueOf(methodName);
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(HttpClientLocalize.methodUnsupported(methodName).get(), e);
        }
    }
}
