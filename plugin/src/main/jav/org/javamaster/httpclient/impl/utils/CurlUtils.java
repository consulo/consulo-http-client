package org.javamaster.httpclient.impl.utils;

import java.util.Set;

public class CurlUtils {
    private static final Set<String> validRequestOptions = Set.of("HEAD", "POST", "PUT", "GET", "DELETE", "PATCH", "OPTIONS");
    private static final Set<String> alwaysSetShortOptions = Set.of("i", "v", "L");
    private static final Set<String> alwaysSetLongOptions = Set.of("verbose", "include", "location", "compressed");
    private static final Set<String> knownLongOptions = Set.of(
        "url",
        "request",
        "header",
        "user",
        "form",
        "data",
        "data-raw",
        "data-binary",
        "data-ascii",
        "data-urlencode"
    );
    private static final Set<Character> knownShortOptions = Set.of('X', 'H', 'u', 'F', 'd');

    public static boolean isKnownLongOption(String longOption) {
        return knownLongOptions.contains(longOption);
    }

    public static boolean isKnownShortOption(String shortOption) {
        return !shortOption.isEmpty() && knownShortOptions.contains(shortOption.charAt(0));
    }

    public static boolean isAlwaysSetLongOption(String longOption) {
        return alwaysSetLongOptions.contains(longOption);
    }

    public static boolean isAlwaysSetShortOption(String shortOption) {
        return alwaysSetShortOptions.contains(shortOption);
    }

    public static boolean isValidRequestOption(String reqOption) {
        return validRequestOptions.contains(reqOption);
    }

    public static boolean isLongOption(String option) {
        return option.length() > 2 && option.startsWith("--");
    }

    public static boolean isShortOption(String option) {
        return option.length() > 1 && option.startsWith("-") && !isLongOption(option);
    }

    public static boolean isCurlString(String string) {
        int len = string.length();
        String correctFirstWord = "curl";
        if (len < 5) {
            return false;
        }

        int pos = 0;
        while (pos < len && string.charAt(pos) <= ' ') {
            ++pos;
        }

        int correctFirstWordIndex = 0;

        while (pos < len && string.charAt(pos) == correctFirstWord.charAt(correctFirstWordIndex)) {
            ++pos;
            ++correctFirstWordIndex;
            if (correctFirstWordIndex == 4) {
                if (pos < len && string.charAt(pos) <= ' ') {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    public static String createCurlStringComment(String curlString) {
        String str = curlString.trim().replaceAll("\r\n", "\n");

        return String.format("%s\n", str);
    }
}
