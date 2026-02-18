package org.javamaster.httpclient.model;

import java.util.List;

/**
 * reqBody type could be String, Pair<ByteArray, String>, MutableList<Pair<ByteArray, String>>
 *
 * @author yudong
 */
public class HttpReqInfo {
    private final Object reqBody;
    private final String environment;
    private final List<PreJsFile> preJsFiles;

    public HttpReqInfo(Object reqBody, String environment, List<PreJsFile> preJsFiles) {
        this.reqBody = reqBody;
        this.environment = environment;
        this.preJsFiles = preJsFiles;
    }

    public Object getReqBody() {
        return reqBody;
    }

    public String getEnvironment() {
        return environment;
    }

    public List<PreJsFile> getPreJsFiles() {
        return preJsFiles;
    }
}
