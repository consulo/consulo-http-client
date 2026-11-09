package consulo.httpClient.execution.runner;

import org.javamaster.httpclient.impl.js.HttpScriptTestResult;
import consulo.localize.LocalizeValue;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.model.HttpResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Outcome of one request run.
 *
 * @param httpInfo   request and response description, shown by the dashboard and printed by the command line client
 * @param response   raw response, null when no response was received
 * @param error      failure before or while sending, or in a handler script
 * @param costMillis time from sending until the whole response was received, 0 when not sent
 * @param tests      {@code client.test} results of the response handler
 * @param paramMap   {@code # @direction} values of the request
 * @param savedFile  absolute path of the {@code >> file} the response was saved to
 * @author VISTALL
 * @since 2026-11-03
 */
public record HttpRequestRunResult(LocalizeValue tabName,
                                   String method,
                                   @Nullable String url,
                                   HttpInfo httpInfo,
                                   @Nullable HttpResponse response,
                                   @Nullable Throwable error,
                                   long costMillis,
                                   List<HttpScriptTestResult> tests,
                                   Map<String, String> paramMap,
                                   @Nullable String savedFile) {
    public @Nullable Integer statusCode() {
        return response == null ? null : response.statusCode();
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean hasFailedTests() {
        for (HttpScriptTestResult test : tests) {
            if (!test.passed()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuccessful() {
        return !hasError() && !hasFailedTests();
    }
}
