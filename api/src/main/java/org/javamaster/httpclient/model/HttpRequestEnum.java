package org.javamaster.httpclient.model;

import consulo.application.Application;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.http.HttpMethod;
import consulo.http.HttpRequestBuilder;
import consulo.http.HttpRequestBuilderFactory;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
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

    public CompletableFuture<HttpResponse> execute(String url,
                                                   consulo.http.HttpVersion version,
                                                   LinkedMultiValueMap<String, String> reqHttpHeaders,
                                                   Object reqBody,
                                                   List<String> httpReqDescList,
                                                   String tabName,
                                                   Map<String, String> paramMap) {
        try {
            Application application = Application.get();

            HttpRequestBuilderFactory requestBuilderFactory = application.getInstance(HttpRequestBuilderFactory.class);

            HttpRequestBuilder builder = requestBuilderFactory.newBuilder(url, myHttpMethod);
            builder.version(version);

            long connectTimeout = paramMap.containsKey(ParamEnum.CONNECT_TIMEOUT_NAME.getParam())
                ? Long.parseLong(paramMap.get(ParamEnum.CONNECT_TIMEOUT_NAME.getParam()))
                : HttpUtilsPart.CONNECT_TIMEOUT;

            builder.connectTimeout((int) connectTimeout);

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

           // long tmpLength = bodyPublisher.contentLength();
            //long contentLength = (tmpLength == -1L) ? multipartLength : tmpLength;
            long contentLength = 0; // TODO body

            httpReqDescList.add("Content-Length: " + contentLength + HttpUtilsPart.CR_LF);

            String size = StringUtil.formatFileSize(contentLength);
            httpReqDescList.add(0, "// " + HttpClientLocalize.reqSize(size).get() + HttpUtilsPart.CR_LF);

            httpReqDescList.add(HttpUtilsPart.CR_LF);

            List<String> descList = HttpUtilsPart.getReqBodyDesc(reqBody);
            httpReqDescList.addAll(descList);

            ApplicationConcurrency concurrency = application.getInstance(ApplicationConcurrency.class);

            CompletableFuture<HttpResponse> future = new CompletableFuture<>();

            concurrency.executor().execute(() -> {
                try {
                    future.complete(builder.connect(it -> new HttpResponse(it.statusCode(), it.statusMessage(), it.responseHeaders())));
                }
                catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            });
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
