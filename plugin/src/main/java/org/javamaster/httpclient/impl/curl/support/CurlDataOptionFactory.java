package org.javamaster.httpclient.impl.curl.support;

import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.impl.curl.data.CurlDataOption;
import org.javamaster.httpclient.impl.curl.data.CurlFileDataOption;
import org.javamaster.httpclient.impl.curl.data.CurlStringDataOption;

import java.nio.charset.StandardCharsets;

public class CurlDataOptionFactory {
    private static final String DATA_URL_ENCODE = "data-urlencode";
    private static final String DATA = "data";
    private static final String DATA_ASCII = "data-ascii";
    private static final String DATA_BINARY = "data-binary";
    private static final String DATA_RAW = "data-raw";

    private CurlDataOptionFactory() {
    }

    public static CurlDataOption getCurlDataOption(String optionName, String data) {
        switch (optionName) {
            case DATA_URL_ENCODE:
                return new CurlStringDataOption(data, StandardCharsets.UTF_8.name());
            case DATA:
            case DATA_ASCII:
            case DATA_BINARY:
                if (!data.startsWith("@")) {
                    return new CurlStringDataOption(data);
                } else {
                    String fileName = data.substring(1);
                    if (StringUtil.isNotEmpty(fileName)) {
                        return new CurlFileDataOption(fileName);
                    }
                }
                return new CurlStringDataOption(data);
            case DATA_RAW:
                return new CurlStringDataOption(data);
            default:
                return null;
        }
    }
}
