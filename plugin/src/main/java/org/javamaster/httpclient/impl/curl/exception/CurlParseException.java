package org.javamaster.httpclient.impl.curl.exception;

import org.javamaster.httpclient.impl.curl.enums.CurlParseErrorType;
import org.javamaster.httpclient.NlsBundle;

import java.net.URISyntaxException;

public class CurlParseException extends RuntimeException {
    private final CurlParseErrorType type;

    public CurlParseException(CurlParseErrorType type, String message, Exception cause) {
        super(message, cause);
        this.type = type;
    }

    public CurlParseException(CurlParseErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public CurlParseErrorType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getClass().getName() + ", type: " + type + ", msg: " + getMessage();
    }

    public static CurlParseException newNotCurlException(String curl) {
        return new CurlParseException(CurlParseErrorType.NOT_CURL, NlsBundle.message("curl.is.not.curl", curl));
    }

    public static CurlParseException newNoUrlException() {
        return new CurlParseException(CurlParseErrorType.NO_URL, NlsBundle.message("curl.no.url"));
    }

    public static CurlParseException newInvalidUrlException(String url, URISyntaxException cause) {
        return new CurlParseException(CurlParseErrorType.INVALID_URL, NlsBundle.message("curl.invalid.url", url), cause);
    }

    public static CurlParseException newInvalidMethodException(String method) {
        return new CurlParseException(CurlParseErrorType.INVALID_HTTP_METHOD, NlsBundle.message("curl.method.not.supported", method));
    }

    public static CurlParseException newNotSupportedOptionException(String option) {
        return new CurlParseException(CurlParseErrorType.UNKNOWN_OPTION, NlsBundle.message("curl.unknown.option", option));
    }

    public static CurlParseException newNoRequiredOptionDataException(String option) {
        return new CurlParseException(CurlParseErrorType.INCOMPLETE_OPTION, NlsBundle.message("curl.incomplete.option", option));
    }

    public static CurlParseException newInvalidHeaderException(String header) {
        return new CurlParseException(CurlParseErrorType.INVALID_HEADER, NlsBundle.message("curl.invalid.header", header));
    }

    public static CurlParseException newInvalidFormDataException(String formData) {
        return new CurlParseException(CurlParseErrorType.INVALID_FORM_DATA, NlsBundle.message("curl.form.data.no.value", formData));
    }
}
