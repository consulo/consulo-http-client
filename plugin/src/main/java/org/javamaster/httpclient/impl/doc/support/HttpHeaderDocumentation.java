package org.javamaster.httpclient.impl.doc.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
public class HttpHeaderDocumentation {
    private static final String CC_LICENSE =
            " is licensed under <a href=\"https://creativecommons.org/licenses/by-sa/2.5/\">CC-BY-SA 2.5</a>.";
    private static final String RFC_PREFIX = "https://tools.ietf.org/html/rfc";

    private final String name;
    private final String myRfc;
    private final String myRfcTitle;
    private final String description;
    private final boolean isDeprecated;

    private HttpHeaderDocumentation(
            String name,
            String myRfc,
            String myRfcTitle,
            String description,
            boolean isDeprecated
    ) {
        this.name = name;
        this.myRfc = myRfc;
        this.myRfcTitle = myRfcTitle;
        this.description = description;
        this.isDeprecated = isDeprecated;
    }

    public HttpHeaderDocumentation(String name) {
        this(name, "", "", "", false);
    }

    public String getName() {
        return name;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public String getUrl() {
        return "https://developer.mozilla.org/en_US/docs/Web/HTTP/Headers/" + name;
    }

    public @Nullable String generateDoc() {
        if (StringUtil.isEmpty(description)) {
            return null;
        }

        StringBuilder sb = new StringBuilder().append(description);

        if (StringUtil.isNotEmpty(myRfc) && StringUtil.isNotEmpty(myRfcTitle)) {
            sb.append("<br/><br/>").append("<a href=\"").append(RFC_PREFIX).append(myRfc)
                    .append("\">").append(myRfcTitle).append("</a>");
        }

        sb.append("<br/><br/>").append("<a href=\"").append(getUrl()).append("\">").append(name)
                .append("</a> by ").append("<a href=\"").append(getUrl()).append("$history").append("\">")
                .append("Mozilla Contributors").append("</a>").append(CC_LICENSE);

        return sb.toString();
    }

    public static @Nullable HttpHeaderDocumentation read(JsonObject obj) {
        String value = getValue(obj, "name");
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        String title = getValue(obj, "rfc-title");
        String ref = getValue(obj, "rfc-ref");
        String descr = getValue(obj, "descr");

        JsonElement obsolete = obj.get("obsolete");
        boolean isDeprecated = obsolete != null && obsolete.isJsonPrimitive() && obsolete.getAsBoolean();

        return new HttpHeaderDocumentation(value, ref, title, descr, isDeprecated);
    }

    private static String getValue(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null) {
            return "";
        }

        if (!element.isJsonPrimitive()) {
            return "";
        }

        return element.getAsString();
    }
}
