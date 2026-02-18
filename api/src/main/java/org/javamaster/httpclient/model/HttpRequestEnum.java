package org.javamaster.httpclient.model;

import com.google.common.net.HttpHeaders;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.utils.HttpUtilsPart;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public enum HttpRequestEnum {
    GET(HttpIcons.GET) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            long readTimeout = paramMap.containsKey(ParamEnum.READ_TIMEOUT_NAME.getParam())
                    ? Long.parseLong(paramMap.get(ParamEnum.READ_TIMEOUT_NAME.getParam()))
                    : HttpUtilsPart.READ_TIMEOUT;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .version(version)
                    .timeout(Duration.ofSeconds(readTimeout))
                    .GET()
                    .uri(URI.create(url));

            setHeaders(reqHeaderMap, builder);

            return builder.build();
        }
    },
    POST(HttpIcons.POST) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            long readTimeout = paramMap.containsKey(ParamEnum.READ_TIMEOUT_NAME.getParam())
                    ? Long.parseLong(paramMap.get(ParamEnum.READ_TIMEOUT_NAME.getParam()))
                    : HttpUtilsPart.READ_TIMEOUT;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .version(version)
                    .timeout(Duration.ofSeconds(readTimeout))
                    .POST(bodyPublisher)
                    .uri(URI.create(url));

            setHeaders(reqHeaderMap, builder);

            return builder.build();
        }
    },
    DELETE(HttpIcons.DELETE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            return buildOtherRequest(name(), url, version, reqHeaderMap, paramMap, bodyPublisher);
        }
    },
    PUT(HttpIcons.PUT) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            long readTimeout = paramMap.containsKey(ParamEnum.READ_TIMEOUT_NAME.getParam())
                    ? Long.parseLong(paramMap.get(ParamEnum.READ_TIMEOUT_NAME.getParam()))
                    : HttpUtilsPart.READ_TIMEOUT;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .version(version)
                    .timeout(Duration.ofSeconds(readTimeout))
                    .PUT(bodyPublisher)
                    .uri(URI.create(url));

            setHeaders(reqHeaderMap, builder);

            return builder.build();
        }
    },
    OPTIONS(HttpIcons.FILE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            return buildOtherRequest(name(), url, version, reqHeaderMap, paramMap, BodyPublishers.noBody());
        }
    },
    PATCH(HttpIcons.FILE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            return buildOtherRequest(name(), url, version, reqHeaderMap, paramMap, bodyPublisher);
        }
    },
    HEAD(HttpIcons.FILE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            return buildOtherRequest(name(), url, version, reqHeaderMap, paramMap, BodyPublishers.noBody());
        }
    },
    TRACE(HttpIcons.FILE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            return buildOtherRequest(name(), url, version, reqHeaderMap, paramMap, BodyPublishers.noBody());
        }
    },
    WEBSOCKET(HttpIcons.WS) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            throw new UnsupportedOperationException();
        }
    },
//    DUBBO(HttpIcons.DUBBO) {
//        @Override
//        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
//                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
//            throw new UnsupportedOperationException();
//        }
//    },
    MOCK_SERVER(HttpIcons.FILE) {
        @Override
        public HttpRequest createRequest(String url, Version version, LinkedMultiValueMap<String, String> reqHeaderMap,
                                          BodyPublisher bodyPublisher, Map<String, String> paramMap) {
            throw new UnsupportedOperationException();
        }
    };

    private final Image icon;

    HttpRequestEnum(Image icon) {
        this.icon = icon;
    }

    public Image getIcon() {
        return icon;
    }

    public void setHeaders(LinkedMultiValueMap<String, String> reqHeaderMap, HttpRequest.Builder builder) {
        reqHeaderMap.forEach((name, values) -> {
            values.forEach(value -> {
                builder.header(name, value);
            });
        });
    }

    public HttpRequest buildOtherRequest(String methodName, String url, Version version,
                                          LinkedMultiValueMap<String, String> reqHeaderMap,
                                          Map<String, String> paramMap, BodyPublisher bodyPublisher) {
        long readTimeout = paramMap.containsKey(ParamEnum.READ_TIMEOUT_NAME.getParam())
                ? Long.parseLong(paramMap.get(ParamEnum.READ_TIMEOUT_NAME.getParam()))
                : HttpUtilsPart.READ_TIMEOUT;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .method(methodName, bodyPublisher)
                .uri(URI.create(url));

        setHeaders(reqHeaderMap, builder);

        return builder.build();
    }

    public CompletableFuture<HttpResponse<byte[]>> execute(String url, Version version,
                                                            LinkedMultiValueMap<String, String> reqHttpHeaders,
                                                            Object reqBody, List<String> httpReqDescList,
                                                            String tabName, Map<String, String> paramMap) {
        try {
            var pair = HttpUtilsPart.convertToReqBodyPublisher(reqBody);

            BodyPublisher bodyPublisher = pair.first;
            long multipartLength = pair.second;

            HttpRequest request = createRequest(url, version, reqHttpHeaders, bodyPublisher, paramMap);

            long connectTimeout = paramMap.containsKey(ParamEnum.CONNECT_TIMEOUT_NAME.getParam())
                    ? Long.parseLong(paramMap.get(ParamEnum.CONNECT_TIMEOUT_NAME.getParam()))
                    : HttpUtilsPart.CONNECT_TIMEOUT;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(connectTimeout))
                    .build();

            String commentTabName = "### " + tabName + HttpUtilsPart.CR_LF;
            httpReqDescList.add(commentTabName);

            httpReqDescList.add(request.method() + " " + request.uri() + " " + HttpUtilsPart.getVersionDesc(version) + HttpUtilsPart.CR_LF);

            request.headers()
                    .map()
                    .forEach((key, values) -> {
                        values.forEach(value -> {
                            httpReqDescList.add(key + ": " + value + HttpUtilsPart.CR_LF);
                        });
                    });

            long tmpLength = bodyPublisher.contentLength();
            long contentLength = (tmpLength == -1L) ? multipartLength : tmpLength;

            httpReqDescList.add(HttpHeaders.CONTENT_LENGTH + ": " + contentLength + HttpUtilsPart.CR_LF);

            String size = StringUtil.formatFileSize(contentLength);
            httpReqDescList.add(0, "// " + NlsBundle.message("req.size", size) + HttpUtilsPart.CR_LF);

            httpReqDescList.add(HttpUtilsPart.CR_LF);

            List<String> descList = HttpUtilsPart.getReqBodyDesc(reqBody);
            httpReqDescList.addAll(descList);

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public abstract HttpRequest createRequest(String url, Version version,
                                               LinkedMultiValueMap<String, String> reqHeaderMap,
                                               BodyPublisher bodyPublisher, Map<String, String> paramMap);

    public static HttpRequestEnum getInstance(String methodName) {
        try {
            return HttpRequestEnum.valueOf(methodName);
        } catch (Exception e) {
            throw new UnsupportedOperationException(NlsBundle.message("method.unsupported", methodName), e);
        }
    }
}
