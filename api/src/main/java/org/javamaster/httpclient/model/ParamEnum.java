package org.javamaster.httpclient.model;

import consulo.language.editor.completion.lookup.AddSpaceInsertHandler;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.LookupElement;
import org.javamaster.httpclient.NlsBundle;

import java.util.*;

/**
 * @author yudong
 */
public enum ParamEnum {
    AUTO_ENCODING("auto-encoding", NlsBundle.message("auto.encoding.desc")),
    NO_LOG("no-log", NlsBundle.message("no.log.desc")),
    CONNECT_TIMEOUT_NAME("connectTimeout", NlsBundle.message("connect.timeout.desc")),
    READ_TIMEOUT_NAME("readTimeout", NlsBundle.message("read.timeout.desc")),
    TIMEOUT_NAME("timeout", NlsBundle.message("timeout.desc")),
    REQUIRE("require", NlsBundle.message("require.desc")),
    IMPORT("import", NlsBundle.message("import.desc")),
    RESPONSE_STATUS("responseStatus", NlsBundle.message("response.status.desc")),
    STATIC_FOLDER("staticFolder", NlsBundle.message("static.folder.desc"));

    private final String param;
    private final String desc;

    ParamEnum(String param, String desc) {
        this.param = param;
        this.desc = desc;
    }

    public String getParam() {
        return param;
    }

    public String getDesc() {
        return desc;
    }

    public InsertHandler<LookupElement> insertHandler() {
        return AddSpaceInsertHandler.INSTANCE;
    }

    private static final Map<String, ParamEnum> map;
    private static final Set<String> filePathParamSet;

    static {
        map = new HashMap<>();
        for (ParamEnum e : values()) {
            map.put(e.param, e);
        }

        filePathParamSet = new HashSet<>(Arrays.asList(
                IMPORT.param,
                STATIC_FOLDER.param
        ));
    }

    public static ParamEnum getEnum(String param) {
        return map.get(param);
    }

    public static List<ParamEnum> getRequestParams() {
        return Arrays.asList(
                CONNECT_TIMEOUT_NAME,
                READ_TIMEOUT_NAME,
                TIMEOUT_NAME,
                RESPONSE_STATUS,
                STATIC_FOLDER,
                AUTO_ENCODING,
                NO_LOG
        );
    }

    public static List<ParamEnum> getGlobalParams() {
        return Collections.singletonList(IMPORT);
    }

    public static boolean isFilePathParam(String paramName) {
        return filePathParamSet.contains(paramName);
    }
}
