package org.javamaster.httpclient.impl.js;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author yudong
 */
public class JsHelper {
    private static boolean jsExecutorInitialed = false;

    private JsHelper() {
    }

    public static void alreadyInit() {
        jsExecutorInitialed = true;
    }

    @Nullable
    public static String getJsGlobalVariable(String name) {
        if (!jsExecutorInitialed) {
            return null;
        }

        Map<String, String> globalVariableMap = JsExecutor.getJsGlobalVariableMap();
        if (globalVariableMap == null) {
            return null;
        }

        return globalVariableMap.get(name);
    }
}
