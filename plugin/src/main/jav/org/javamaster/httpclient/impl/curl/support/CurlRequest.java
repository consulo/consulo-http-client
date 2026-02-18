package org.javamaster.httpclient.impl.curl.support;

import consulo.util.lang.StringUtil;
import org.apache.http.cookie.Cookie;
import org.javamaster.httpclient.impl.curl.data.CurlAuthData;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurlRequest {
    private List<Biscuit> biscuits = new ArrayList<>();
    public String httpMethod;
    public String urlBase;
    public String urlPath;
    public List<KeyValuePair> headers = new ArrayList<>();
    public List<KeyValuePair> parameters = new ArrayList<>();
    public boolean haveTextToSend;
    public boolean haveFileToSend;
    public boolean isFileUpload;
    public String textToSend;
    public String filesToSend;

    public List<CurlFormBodyPart> formBodyPart = new ArrayList<>();

    public String multipartBoundary;

    public CurlAuthData authData;

    private List<File> files;
    private String url;

    public String getHeaderValue(String name, String defaultValue) {
        for (KeyValuePair header : headers) {
            if (name.equals(header.key)) {
                return header.value;
            }
        }
        return defaultValue;
    }

    public List<String> getHeadersValue(String name) {
        List<String> list = new ArrayList<>();

        for (KeyValuePair header : headers) {
            if (name.equals(header.key)) {
                list.add(header.value);
            }
        }

        return list.isEmpty() ? Collections.emptyList() : list;
    }

    public void deleteHeader(String name) {
        headers.removeIf(it -> name.equals(it.key));
    }

    public List<File> getFiles() {
        if (files == null) {
            files = new ArrayList<>();
            for (String path : StringUtil.split(filesToSend, File.pathSeparator)) {
                files.add(new File(path));
            }
        }
        return files;
    }

    private String getUrl() {
        if (url == null) {
            String base = urlBase;

            if (!base.endsWith("/") && !urlPath.isEmpty()) {
                base = base + "/";
            }

            base = urlPath.startsWith("/") ? base + urlPath.substring(1) : base + urlPath;

            url = base.replace(" ", "%20");
        }
        return url;
    }

    private String createQueryString() {
        return StringUtil.join(
                parameters,
                it -> {
                    try {
                        String key = URLEncoder.encode(it.key, "UTF-8");
                        String value = URLEncoder.encode(it.value, "UTF-8");
                        return key + "=" + value;
                    } catch (UnsupportedEncodingException var3) {
                        return "";
                    }
                }, "&"
        );
    }

    public void addBiscuit(Cookie cookie) {
        Date date = cookie.getExpiryDate();

        biscuits.add(
                new Biscuit(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), date != null ? date.getTime() : -1L)
        );
    }

    public boolean isEmptyCredentials() {
        return authData == CurlAuthData.EMPTY_CREDENTIALS;
    }

    public void setEmptyCredentials() {
        authData = CurlAuthData.EMPTY_CREDENTIALS;
    }

    @Override
    public String toString() {
        String queryString = createQueryString();
        String urlStr = getUrl();

        return !queryString.isEmpty() ? urlStr + (urlStr.contains("?") ? "&" : "?") + queryString : urlStr;
    }

    public static class KeyValuePair {
        public String key;
        public String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Biscuit {
        public String name;
        public String value;
        public String domain;
        public String path;
        public long date;

        public Biscuit(String name, String value, String domain, String path, long date) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.path = path;
            this.date = date;
        }
    }
}
