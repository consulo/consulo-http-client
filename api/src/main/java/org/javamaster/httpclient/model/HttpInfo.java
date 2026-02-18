package org.javamaster.httpclient.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author yudong
 */
public class HttpInfo {
    private final List<String> httpReqDescList;
    private final List<String> httpResDescList;
    private final SimpleTypeEnum type;
    private final byte[] byteArray;
    private final Throwable httpException;
    private String contentType;

    public HttpInfo(List<String> httpReqDescList, List<String> httpResDescList,
                    SimpleTypeEnum type, byte[] byteArray, Throwable httpException, String contentType) {
        this.httpReqDescList = httpReqDescList;
        this.httpResDescList = httpResDescList;
        this.type = type;
        this.byteArray = byteArray;
        this.httpException = httpException;
        this.contentType = contentType;
    }

    public HttpInfo(List<String> httpReqDescList, List<String> httpResDescList,
                    SimpleTypeEnum type, byte[] byteArray, Throwable httpException) {
        this(httpReqDescList, httpResDescList, type, byteArray, httpException, null);
    }

    public List<String> getHttpReqDescList() {
        return httpReqDescList;
    }

    public List<String> getHttpResDescList() {
        return httpResDescList;
    }

    public SimpleTypeEnum getType() {
        return type;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public Throwable getHttpException() {
        return httpException;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        HttpInfo httpInfo = (HttpInfo) other;

        if (!Objects.equals(httpReqDescList, httpInfo.httpReqDescList)) return false;
        if (!Objects.equals(httpResDescList, httpInfo.httpResDescList)) return false;
        if (type != httpInfo.type) return false;
        if (!Arrays.equals(byteArray, httpInfo.byteArray)) return false;
        if (!Objects.equals(httpException, httpInfo.httpException)) return false;
        return Objects.equals(contentType, httpInfo.contentType);
    }

    @Override
    public int hashCode() {
        int result = httpReqDescList != null ? httpReqDescList.hashCode() : 0;
        result = 31 * result + (httpResDescList != null ? httpResDescList.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(byteArray);
        result = 31 * result + (httpException != null ? httpException.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        return result;
    }
}
