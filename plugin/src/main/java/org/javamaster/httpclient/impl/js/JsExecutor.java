package org.javamaster.httpclient.impl.js;

import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.lang.Trinity;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpReqInfo;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.psi.HttpScriptBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
public interface JsExecutor {
    String getRequestVariable(String key);

    String getJsGlobalVariable(String key);

    Map<String, String> getJsGlobalVariables();

    void initJsRequestObj(
        HttpReqInfo reqInfo,
        HttpRequestEnum method,
        LinkedMultiValueMap<String, String> reqHeaderMap,
        String selectedEnv,
        LinkedHashMap<String, String> fileScopeVariableMap
    );

    List<String> evalJsBeforeRequest(List<PreJsFile> preJsFiles, List<HttpScriptBody> jsListBeforeReq);

    LinkedMultiValueMap<String, String> getHeaderMap();

    String evalJsAfterRequest(
        HttpScriptBody jsScript,
        Trinity<SimpleTypeEnum, byte[], String> triple,
        int statusCode,
        Map<String, List<String>> headerMap
    );

    // TODO !
    static JsExecutor create(Project project, PsiFile httpFile, String tabName) {
        return new JsExecutor() {
            @Override
            public String getRequestVariable(String key) {
                return null;
            }

            @Override
            public String getJsGlobalVariable(String key) {
                return null;
            }

            @Override
            public Map<String, String> getJsGlobalVariables() {
                return null;
            }

            @Override
            public void initJsRequestObj(HttpReqInfo reqInfo, HttpRequestEnum method, LinkedMultiValueMap<String, String> reqHeaderMap, String selectedEnv, LinkedHashMap<String, String> fileScopeVariableMap) {

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
            public String evalJsAfterRequest(HttpScriptBody jsScript, Trinity<SimpleTypeEnum, byte[], String> triple, int statusCode, Map<String, List<String>> headerMap) {
                return null;
            }
        };
    }
}
