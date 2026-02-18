package org.javamaster.httpclient.impl.curl.data;

import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.impl.curl.support.CurlRequest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CurlFormData {
    private final List<CurlRequest.KeyValuePair> headers = new ArrayList<>();

    private ContentType formContentType = ContentType.WILDCARD;
    private File file = null;

    private String name;
    private String content;

    private boolean hasFileContent = false;

    public CurlFormData(String formData) {
        String[] parts = formData.split(";");

        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            String[] split = part.split("=");

            if (index == 0) {
                name = split[0];

                if (split.length > 1) {
                    parseContent(split[1]);
                }

                continue;
            }

            parseAdditionalOption(split);
        }
    }

    private void parseContent(String contentString) {
        if (contentString.startsWith("@")) {
            content = contentString.substring(1);

            hasFileContent = true;

            file = new File(content);
        } else {
            content = contentString;

            hasFileContent = false;
        }
    }

    private void parseAdditionalOption(String[] additionalFormData) {
        String additionalOptionKey = additionalFormData[0];

        if (additionalFormData.length <= 1) {
            return;
        }

        String additionalOptionValue = additionalFormData[1].replaceAll("^\"|\"$|^'|'$", "");

        if (additionalOptionKey.equals("filename") && hasFileContent) {
            file = new File(file.getParent(), additionalOptionValue);
        } else if (additionalOptionKey.equals("type")) {
            formContentType = ContentType.create(additionalOptionValue, StandardCharsets.UTF_8);
        } else if (additionalOptionKey.equals("headers")) {
            int colonPosition = additionalOptionValue.indexOf(':');
            if (colonPosition < 0) {
                headers.add(new CurlRequest.KeyValuePair(additionalOptionValue, ""));
            } else {
                headers.add(
                        new CurlRequest.KeyValuePair(
                                additionalOptionValue.substring(0, colonPosition),
                                additionalOptionValue.substring(colonPosition + 1).trim()
                        )
                );
            }
        }
    }

    public List<CurlRequest.KeyValuePair> getHeaders() {
        return headers;
    }

    public ContentType getFormContentType() {
        return formContentType;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public boolean isHasFileContent() {
        return hasFileContent;
    }
}
