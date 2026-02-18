package org.javamaster.httpclient.impl.js;

import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.impl.utils.HttpUtils;

import java.util.Collections;
import java.util.List;

/**
 * Collect js executed log
 */
public class GlobalLog {
    private static String tabName;
    private static final LinkedMultiValueMap<String, String> logsMap = new LinkedMultiValueMap<>();

    public GlobalLog() {
    }

    public static void setTabName(String tmpKey) {
        tabName = tmpKey;
    }

    public static void log(String msg) {
        String message = msg != null ? msg : "null";
        logsMap.add(tabName, message);
    }

    public static String getAndClearLogs() {
        List<String> logs = logsMap.get(tabName);
        if (logs == null) {
            logs = Collections.emptyList();
        }

        clearLogs();

        return String.join(HttpUtils.CR_LF, logs);
    }

    public static void clearLogs() {
        logsMap.remove(tabName);
        tabName = null;
    }
}
