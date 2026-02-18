package org.javamaster.httpclient.model;

import consulo.http.HttpMethod;
import consulo.restClient.localize.RestClientLocalize;
import consulo.ui.image.Image;
import org.javamaster.httpclient.HttpIcons;

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

    //TODO
//    public CompletableFuture<byte[]> execute(String url, consulo.http.HttpVersion version,
//                                                           LinkedMultiValueMap<String, String> reqHttpHeaders,
//                                                           Object reqBody, List<String> httpReqDescList,
//                                                           String tabName, Map<String, String> paramMap) {
//        try {
//            var pair = HttpUtilsPart.convertToReqBodyPublisher(reqBody);
//
//            BodyPublisher bodyPublisher = pair.first;
//            long multipartLength = pair.second;
//
//            HttpRequest request = createRequest(url, version, reqHttpHeaders, bodyPublisher, paramMap);
//
//            long connectTimeout = paramMap.containsKey(ParamEnum.CONNECT_TIMEOUT_NAME.getParam())
//                ? Long.parseLong(paramMap.get(ParamEnum.CONNECT_TIMEOUT_NAME.getParam()))
//                : HttpUtilsPart.CONNECT_TIMEOUT;
//
//            HttpClient client = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofSeconds(connectTimeout))
//                .build();
//
//            String commentTabName = "### " + tabName + HttpUtilsPart.CR_LF;
//            httpReqDescList.add(commentTabName);
//
//            httpReqDescList.add(request.method() + " " + request.uri() + " " + HttpUtilsPart.getVersionDesc(version) + HttpUtilsPart.CR_LF);
//
//            request.headers()
//                .map()
//                .forEach((key, values) -> {
//                    values.forEach(value -> {
//                        httpReqDescList.add(key + ": " + value + HttpUtilsPart.CR_LF);
//                    });
//                });
//
//            long tmpLength = bodyPublisher.contentLength();
//            long contentLength = (tmpLength == -1L) ? multipartLength : tmpLength;
//
//            httpReqDescList.add(HttpHeaders.CONTENT_LENGTH + ": " + contentLength + HttpUtilsPart.CR_LF);
//
//            String size = StringUtil.formatFileSize(contentLength);
//            httpReqDescList.add(0, "// " + NlsBundle.message("req.size", size) + HttpUtilsPart.CR_LF);
//
//            httpReqDescList.add(HttpUtilsPart.CR_LF);
//
//            List<String> descList = HttpUtilsPart.getReqBodyDesc(reqBody);
//            httpReqDescList.addAll(descList);
//
//            return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
//        }
//        catch (Throwable e) {
//            return CompletableFuture.failedFuture(e);
//        }
//    }

    public static HttpRequestEnum getInstance(String methodName) {
        try {
            return HttpRequestEnum.valueOf(methodName);
        }
        catch (Exception e) {
            throw new UnsupportedOperationException(RestClientLocalize.methodUnsupported(methodName).get(), e);
        }
    }
}
