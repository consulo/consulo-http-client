package org.javamaster.httpclient.impl.completion.support;

import com.google.common.net.HttpHeaders;
import com.google.common.net.HttpHeaders.ReferrerPolicyValues;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import consulo.util.io.FileUtil;
import org.apache.http.entity.ContentType;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.doc.support.HttpHeaderDocumentation;
import org.javamaster.httpclient.utils.DubboUtilsPart;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author yudong
 */
public class HttpHeadersDictionary {
    private static final List<String> ENCODING_VALUES = Arrays.asList(
        "compress",
        "deflate",
        "exi",
        "gzip",
        "identity",
        "pack200-gzip",
        "br",
        "bzip2",
        "lzma",
        "peerdist",
        "sdch",
        "xpress",
        "xz"
    );

    private static final List<String> PREDEFINED_MIME_VARIANTS = Arrays.asList(
        "application/json",
        "application/xml",
        "application/x-yaml",
        "application/graphql",
        "application/atom+xml",
        "application/xhtml+xml",
        "application/svg+xml",
        "application/sql",
        "application/pdf",
        "application/zip",
        "application/x-www-form-urlencoded",
        "multipart/form-data",
        "application/octet-stream",
        "text/plain",
        "text/xml",
        "text/html",
        "text/json",
        "text/csv",
        "image/png",
        "image/jpeg",
        "image/gif",
        "image/webp",
        "image/svg+xml",
        "audio/mpeg",
        "audio/vorbis",
        "text/event-stream",
        "application/stream+json",
        "application/x-ndjson",
        ContentType.MULTIPART_FORM_DATA.getMimeType() + "; boundary=----WebBoundary"
    );

    private static final List<String> SEC_WEBSOCKET_PROTOCOL_VALUES = Arrays.asList(
        "graphql-ws", "subscriptions-transport-ws", "aws-app-sync"
    );

    private static final List<String> REFERRER_POLICY_VALUES;

    static {
        List<String> values = new ArrayList<>();
        Field[] fields = ReferrerPolicyValues.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                values.add((String) field.get(null));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        REFERRER_POLICY_VALUES = Collections.unmodifiableList(values);
    }

    private static final List<String> KNOWN_EXTRA_HEADERS = Arrays.asList(
        "X-Correlation-ID",
        "X-Csrf-Token",
        "X-Forwarded-For",
        "X-Forwarded-Host",
        "X-Forwarded-Proto",
        "X-Http-Method-Override",
        "X-Request-ID",
        "X-Requested-With",
        "X-Total-Count",
        "X-User-Agent",
        "Admin-Token",
        HttpHeaders.REFERRER_POLICY
    );

    private static final Map<String, HttpHeaderDocumentation> HEADER_MAP;

    static {
        Map<String, HttpHeaderDocumentation> map = createMapFromFile();

        for (String header : KNOWN_EXTRA_HEADERS) {
            map.put(header, new HttpHeaderDocumentation(header));
        }

        HEADER_MAP = Collections.unmodifiableMap(map);
    }

    private static final List<String> DUBBO_HEADER_NAMES = Arrays.asList(
        HttpHeaders.CONTENT_TYPE,
        DubboUtilsPart.INTERFACE_KEY,
        DubboUtilsPart.INTERFACE_NAME,
        DubboUtilsPart.METHOD_KEY,
        DubboUtilsPart.VERSION,
        DubboUtilsPart.REGISTRY
    );

    private static final Map<String, List<String>> HEADER_VALUES_MAP;

    static {
        Map<String, List<String>> map = new HashMap<>();
        map.put(HttpHeaders.ACCEPT_ENCODING, ENCODING_VALUES);
        map.put(HttpHeaders.CONTENT_TYPE, PREDEFINED_MIME_VARIANTS);
        map.put(HttpHeaders.ACCEPT, PREDEFINED_MIME_VARIANTS);
        map.put(HttpHeaders.REFERRER_POLICY, REFERRER_POLICY_VALUES);
        map.put(HttpHeaders.SEC_WEBSOCKET_PROTOCOL, SEC_WEBSOCKET_PROTOCOL_VALUES);
        HEADER_VALUES_MAP = Collections.unmodifiableMap(map);
    }

    public static Map<String, HttpHeaderDocumentation> getHeaderMap() {
        return HEADER_MAP;
    }

    public static List<String> getDubboHeaderNames() {
        return DUBBO_HEADER_NAMES;
    }

    public static Map<String, List<String>> getHeaderValuesMap() {
        return HEADER_VALUES_MAP;
    }

    public static HttpHeaderDocumentation getDocumentation(String fieldName) {
        return HEADER_MAP.get(fieldName);
    }

    private static Map<String, HttpHeaderDocumentation> createMapFromFile() {
        String name = "doc/header-documentation_" + NlsBundle.getLang() + ".json";
        try {
            InputStream stream = HttpHeadersDictionary.class.getClassLoader().getResourceAsStream(name);
            if (stream == null) {
                return new HashMap<>();
            }

            String jsonText = FileUtil.loadTextAndClose(stream);

            JsonElement jsonElement = JsonParser.parseString(jsonText);

            if (!jsonElement.isJsonArray()) {
                return new HashMap<>();
            }

            Map<String, HttpHeaderDocumentation> map = new HashMap<>();

            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (!element.isJsonObject()) {
                    continue;
                }

                HttpHeaderDocumentation documentation = HttpHeaderDocumentation.read(element.getAsJsonObject());
                if (documentation == null) {
                    continue;
                }

                map.put(documentation.getName(), documentation);
            }

            return map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
