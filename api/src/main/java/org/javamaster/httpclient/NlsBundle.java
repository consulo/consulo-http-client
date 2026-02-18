package org.javamaster.httpclient;

import consulo.annotation.internal.MigratedExtensionsTo;
import consulo.application.CommonBundle;
import consulo.restClient.localize.RestClientLocalize;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

@MigratedExtensionsTo(RestClientLocalize.class)
@Deprecated
public class NlsBundle {
    public static final NlsBundle INSTANCE = new NlsBundle();

    @NonNls
    private static final String BUNDLE = "org.javamaster.httpclient.nls.HttpClientBundle";

    private static String lang;

    public static String getLang() {
        if (lang == null) {
            Locale locale = ResourceBundle.getBundle(BUNDLE).getLocale();
            lang = (locale == Locale.CHINESE) ? "zh" : "en";
        }
        return lang;
    }

    public static @Nls String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.messageOrDefault(ResourceBundle.getBundle(BUNDLE), key, "", params);
    }

    private NlsBundle() {
    }
}
