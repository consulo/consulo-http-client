package org.javamaster.httpclient.impl.curl.data;

import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;

import java.io.File;

public class CurlFileDataOption implements CurlDataOption {
    private final String myFilename;

    public CurlFileDataOption(String myFilename) {
        this.myFilename = myFilename;
    }

    @Override
    public void apply(CurlRequest curlRequest) {
        if (StringUtil.isNotEmpty(curlRequest.getFilesToSend())) {
            curlRequest.setFilesToSend(curlRequest.getFilesToSend() + File.pathSeparator + myFilename);
        } else {
            curlRequest.setFilesToSend(myFilename);
        }

        curlRequest.setHaveFileToSend(true);
    }
}
