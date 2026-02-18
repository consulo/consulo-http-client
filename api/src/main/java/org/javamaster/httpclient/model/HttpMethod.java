package org.javamaster.httpclient.model;

import consulo.ui.image.Image;
import org.javamaster.httpclient.HttpIcons;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum HttpMethod {
    GET(HttpIcons.GET),
    POST(HttpIcons.POST),
    PUT(HttpIcons.PUT),
    DELETE(HttpIcons.DELETE),
    OPTIONS(HttpIcons.FILE),
    PATCH(HttpIcons.FILE),
    HEAD(HttpIcons.FILE),
    TRACE(HttpIcons.FILE),
    REQUEST(HttpIcons.FILE),
    UNKNOWN(HttpIcons.FILE);

    private final Image icon;

    HttpMethod(Image icon) {
        this.icon = icon;
    }

    public Image getIcon() {
        return icon;
    }

    public static HttpMethod parse(Object method) {
        try {
            if (method instanceof HttpMethod) {
                return (HttpMethod) method;
            }

            return valueOf(method.toString());
        } catch (Exception ignore) {
            return REQUEST;
        }
    }

    public static List<HttpMethod> getMethods() {
        return Arrays.stream(values())
                .filter(it -> it != UNKNOWN && it != REQUEST)
                .collect(Collectors.toList());
    }
}
