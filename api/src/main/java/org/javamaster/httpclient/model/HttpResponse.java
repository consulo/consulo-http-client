package org.javamaster.httpclient.model;

import consulo.http.HttpVersion;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
public record HttpResponse(int statusCode,
                           String statusLine,
                           Map<String, List<String>> headers,
                           String uri,
                           HttpVersion version,
                           byte[] body) {
}
