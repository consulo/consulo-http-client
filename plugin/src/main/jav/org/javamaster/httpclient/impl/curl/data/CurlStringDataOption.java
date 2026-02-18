package org.javamaster.httpclient.impl.curl.data;

import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CurlStringDataOption implements CurlDataOption {
    private final String myData;

    public CurlStringDataOption(String data) {
        this.myData = data;
    }

    public CurlStringDataOption(String data, String encoding) {
        this.myData = encodeData(data, encoding);
    }

    @Override
    public void apply(CurlRequest curlRequest) {
        if (StringUtil.isNotEmpty(curlRequest.getTextToSend())) {
            curlRequest.setTextToSend(curlRequest.getTextToSend() + "&" + myData);
        } else {
            curlRequest.setTextToSend(myData);
        }

        curlRequest.setHaveTextToSend(true);
    }

    private static String encodeData(String data, String encoding) {
        String content = data;
        String name = null;

        if (data.contains("=")) {
            if (data.indexOf("=") == 0) {
                content = data.substring(1);
            } else {
                String[] nameContent = data.split("=", 2);
                name = nameContent[0];
                content = nameContent[1];
            }
        }

        try {
            String encodedData = URLEncoder.encode(content, encoding);

            return name != null ? name + "=" + encodedData : encodedData;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
