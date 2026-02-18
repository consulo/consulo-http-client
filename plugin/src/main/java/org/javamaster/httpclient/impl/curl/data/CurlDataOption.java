package org.javamaster.httpclient.impl.curl.data;

import org.javamaster.httpclient.impl.curl.support.CurlRequest;

public interface CurlDataOption {
    void apply(CurlRequest curlRequest);
}
