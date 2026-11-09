package org.javamaster.httpclient.impl.js;

import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.Trinity;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpReqInfo;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Executes handler scripts of one request run. Implementations are provided via {@link JsExecutorFactory}.
 *
 * @author yudong
 */
public interface JsExecutor {
    JsExecutor NONE = new JsExecutor() {
        @Override
        public @Nullable String getRequestVariable(String key) {
            return null;
        }

        @Override
        public @Nullable String getJsGlobalVariable(String key) {
            return null;
        }

        @Override
        public Map<String, String> getJsGlobalVariables() {
            return Map.of();
        }

        @Override
        public void initJsRequestObj(HttpReqInfo reqInfo,
                                     HttpRequestEnum method,
                                     LinkedMultiValueMap<String, String> reqHeaderMap,
                                     @Nullable String selectedEnv,
                                     Map<String, String> fileScopeVariableMap) {
        }

        @Override
        public List<String> evalJsBeforeRequest(List<PreJsFile> preJsFiles, List<HttpScriptBody> jsListBeforeReq) {
            return List.of();
        }

        @Override
        public LinkedMultiValueMap<String, String> getHeaderMap() {
            return new LinkedMultiValueMap<>();
        }

        @Override
        public @Nullable String evalJsAfterRequest(@Nullable HttpScriptBody jsScript,
                                                   Trinity<SimpleTypeEnum, byte[], String> triple,
                                                   int statusCode,
                                                   Map<String, List<String>> headerMap) {
            return null;
        }

        @Override
        public List<HttpScriptTestResult> getTestResults() {
            return List.of();
        }
    };

    /**
     * {@code request.variables.get(key)} set by pre-request scripts.
     */
    @Nullable
    String getRequestVariable(String key);

    /**
     * {@code client.global.get(key)}.
     */
    @Nullable
    String getJsGlobalVariable(String key);

    Map<String, String> getJsGlobalVariables();

    void initJsRequestObj(HttpReqInfo reqInfo,
                          HttpRequestEnum method,
                          LinkedMultiValueMap<String, String> reqHeaderMap,
                          @Nullable String selectedEnv,
                          Map<String, String> fileScopeVariableMap);

    /**
     * {@code request.url}: the raw URL of the request and a substitutor for {@code tryGetSubstituted()}.
     * Called before {@link #evalJsBeforeRequest}.
     */
    default void initRequestUrl(String rawUrl, UnaryOperator<String> substitutor) {
    }

    /**
     * Runs {@code # @import} files, the global and the pre-request handlers.
     *
     * @return request description lines with script output
     */
    List<String> evalJsBeforeRequest(List<PreJsFile> preJsFiles, List<HttpScriptBody> jsListBeforeReq);

    /**
     * Headers set by pre-request scripts via {@code request.headers.set/add}.
     */
    LinkedMultiValueMap<String, String> getHeaderMap();

    /**
     * Runs the response handler.
     *
     * @return script output, null when there is no handler
     */
    @Nullable
    String evalJsAfterRequest(@Nullable HttpScriptBody jsScript,
                              Trinity<SimpleTypeEnum, byte[], String> triple,
                              int statusCode,
                              Map<String, List<String>> headerMap);

    /**
     * {@code client.test} results of the last {@link #evalJsAfterRequest} call.
     */
    List<HttpScriptTestResult> getTestResults();

    static JsExecutor create(Project project, HttpFile httpFile, LocalizeValue tabName) {
        for (JsExecutorFactory factory : project.getApplication().getExtensionList(JsExecutorFactory.class)) {
            return factory.create(project, httpFile, tabName);
        }
        return NONE;
    }
}
