package org.javamaster.httpclient.model;

import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
public record HttpResponse(int statusCode, String statusLine, Map<String, String> headers) {
}
