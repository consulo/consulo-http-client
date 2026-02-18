package org.javamaster.httpclient.psi;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-01-18
 */
public record HttpContentType(String mimeType, Charset charset, List<HttpContentTypeParam> params) {
}
