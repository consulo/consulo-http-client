package org.javamaster.httpclient.model;

import consulo.http.HttpMethod;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.ui.image.Image;
import org.javamaster.httpclient.HttpIcons;


public enum HttpRequestEnum {
    GET(HttpMethod.GET),
    POST(HttpMethod.POST),
    DELETE(HttpMethod.DELETE),
    PUT(HttpMethod.PUT),
    OPTIONS(HttpMethod.OPTIONS),
    PATCH(HttpMethod.PATCH),
    HEAD(HttpMethod.HEAD),
    TRACE(HttpMethod.TRACE),
    WEBSOCKET(HttpMethod.GET),
    //    DUBBO(HttpIcons.DUBBO),
    MOCK_SERVER(HttpMethod.GET);

    private final HttpMethod myHttpMethod;

    HttpRequestEnum(HttpMethod httpMethod) {
        myHttpMethod = httpMethod;
    }

    /**
     * Computed on demand: the enum is also used outside of the UI (command line client), where icons are not available.
     */
    public Image getIcon() {
        return switch (this) {
            case GET -> HttpIcons.GET;
            case POST -> HttpIcons.POST;
            case DELETE -> HttpIcons.DELETE;
            case PUT -> HttpIcons.PUT;
            case WEBSOCKET -> HttpIcons.WS;
            default -> HttpIcons.FILE;
        };
    }

    public HttpMethod getHttpMethod() {
        return myHttpMethod;
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
