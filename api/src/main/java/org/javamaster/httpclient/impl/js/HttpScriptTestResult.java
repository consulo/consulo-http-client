package org.javamaster.httpclient.impl.js;

import org.jspecify.annotations.Nullable;

/**
 * Result of one {@code client.test(name, fn)} call in a response handler.
 *
 * @param message failure message, null when the test passed
 * @author VISTALL
 * @since 2026-11-03
 */
public record HttpScriptTestResult(String name, boolean passed, @Nullable String message) {
    public static HttpScriptTestResult passed(String name) {
        return new HttpScriptTestResult(name, true, null);
    }

    public static HttpScriptTestResult failed(String name, String message) {
        return new HttpScriptTestResult(name, false, message);
    }
}
