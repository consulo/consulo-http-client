package org.javamaster.httpclient.impl.js;

import consulo.document.FileDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.annos.JsBridge;
import org.javamaster.httpclient.impl.exception.HttpFileException;
import org.javamaster.httpclient.impl.exception.JsFileException;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpReqInfo;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.mozilla.javascript.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.javamaster.httpclient.impl.resolve.VariableResolver.ENV_PREFIX;
import static org.javamaster.httpclient.impl.resolve.VariableResolver.PROPERTY_PREFIX;
import static org.javamaster.httpclient.impl.utils.HttpUtils.CR_LF;
import static org.javamaster.httpclient.impl.utils.HttpUtils.gson;

/**
 * Execute the previous and post request js scripts (always executed in the EDT thread)
 *
 * @author yudong
 */
public class JsExecutor {
    private static final Map<String, ScriptableObject> libraryLoadedMap = new HashMap<>();
    private static Context context;
    private static ScriptableObject global;
    private static String javaBridgeJsStr;
    private static String initRequestJsStr;
    private static DocumentBuilderFactory documentBuilderFactory;
    private static XPathFactory xPathFactory;
    private static Map<String, String> jsGlobalVariableMap;

    private final Project project;
    private final PsiFile httpFile;
    private final String tabName;
    private final ScriptableObject reqScriptableObject;
    private final JavaBridge originalJavaBridge;

    private byte[] bodyArray;
    private String jsonStr;
    private Document xmlDoc;
    private XPath xPath;

    public JsExecutor(Project project, PsiFile httpFile, String tabName) {
        this.project = project;
        this.httpFile = httpFile;
        this.tabName = tabName;

        ensureInitialized();

        ScriptableObject scriptableObject = context.initStandardObjects();
        scriptableObject.setPrototype(global);

        // Register js bridge object javaBridge
        this.originalJavaBridge = new JavaBridge(this);
        Object javaBridge = Context.javaToJS(originalJavaBridge, scriptableObject);
        ScriptableObject.putProperty(scriptableObject, "javaBridge", javaBridge);

        context.evaluateString(scriptableObject, javaBridgeJsStr, "javaBridge.js", 1, null);

        context.evaluateString(scriptableObject, initRequestJsStr, "initRequest.js", 1, null);

        String jsonJs = PROPERTY_PREFIX + " = " + gson.toJson(System.getProperties()) + ";\n" +
                ENV_PREFIX + " = " + gson.toJson(System.getenv()) + ";";
        context.evaluateString(scriptableObject, jsonJs, "initPropertiesAndEnv.js", 1, null);

        this.reqScriptableObject = scriptableObject;
    }

    private static synchronized void ensureInitialized() {
        if (context != null) {
            return;
        }

        context = Context.enter();
        global = (ScriptableObject) context.initStandardObjects();

        global.defineProperty("CONTENT_TRUNCATED", NlsBundle.message("content.truncated"), ScriptableObject.READONLY);
        global.defineProperty("GLOBAL_SET", NlsBundle.message("value.global.set"), ScriptableObject.READONLY);
        global.defineProperty("REQUEST_SET", NlsBundle.message("value.req.set"), ScriptableObject.READONLY);

        try {
            String jsStr = JsExecutor.class.getClassLoader().getResource("js/crypto-js.js").openStream()
                    .readAllBytes().toString();
            context.evaluateString(global, jsStr, "crypto-js.js", 1, null);
            // Register CryptoJS object
            global.getPrototype().put("CryptoJS", global, global.get("CryptoJS"));

            Object globalLog = Context.javaToJS(new GlobalLog(), global);
            ScriptableObject.putProperty(global, "globalLog", globalLog);

            jsStr = new String(JsExecutor.class.getClassLoader().getResource("js/initGlobal.js")
                    .openStream().readAllBytes(), StandardCharsets.UTF_8);
            context.evaluateString(global, jsStr, "initGlobal.js", 1, null);

            JsHelper.alreadyInit();

            // Initialize javaBridgeJsStr
            StringBuilder sb = new StringBuilder();
            for (Method method : JavaBridge.class.getDeclaredMethods()) {
                JsBridge jsBridge = method.getAnnotation(JsBridge.class);
                if (jsBridge == null) {
                    continue;
                }

                sb.append("function ").append(jsBridge.jsFun()).append(" {\n");
                sb.append("    return javaBridge.").append(jsBridge.jsFun()).append(";\n");
                sb.append("}\n");
            }
            javaBridgeJsStr = sb.toString();

            // Initialize initRequestJsStr
            initRequestJsStr = new String(JsExecutor.class.getClassLoader().getResource("js/initRequest.js")
                    .openStream().readAllBytes(), StandardCharsets.UTF_8);

            // Initialize documentBuilderFactory
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            // Initialize xPathFactory
            xPathFactory = XPathFactory.newInstance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Project getProject() {
        return project;
    }

    public PsiFile getHttpFile() {
        return httpFile;
    }

    public ScriptableObject getReqScriptableObject() {
        return reqScriptableObject;
    }

    public byte[] getBodyArray() {
        return bodyArray;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public Document getXmlDoc() {
        return xmlDoc;
    }

    public XPath getXPath() {
        return xPath;
    }

    public static Context getContext() {
        return context;
    }

    public static XPathFactory getXPathFactory() {
        return xPathFactory;
    }

    public static Map<String, String> getJsGlobalVariableMap() {
        return jsGlobalVariableMap;
    }

    public void initJsRequestObj(
            HttpReqInfo reqInfo,
            HttpRequestEnum method,
            LinkedMultiValueMap<String, String> reqHeaderMap,
            String selectedEnv,
            LinkedHashMap<String, String> fileScopeVariableMap
    ) {
        String environment = reqInfo.getEnvironment();

        String headers = gson.toJson(reqHeaderMap);

        String globalVariables = gson.toJson(fileScopeVariableMap);

        Object jsBody = HttpUtils.convertReqBody(reqInfo.getReqBody());

        String js;
        if (jsBody instanceof String) {
            js = "request.body = {\n" +
                    "    string: () => { return " + jsBody + "; },\n" +
                    "    tryGetSubstituted: () => { return " + jsBody + "; },\n" +
                    "    bytes: () => { return javaBridge.convertBodyToByteArray(" + jsBody + ") }\n" +
                    "};";
        } else {
            bodyArray = (byte[]) jsBody;

            js = "request.body = {\n" +
                    "    string: () => { return javaBridge.getBodyString(); },\n" +
                    "    tryGetSubstituted: () => { return javaBridge.getBodyString(); },\n" +
                    "    bytes: () => { return javaBridge.getBodyArray(); }\n" +
                    "};";
        }

        js += "request.method = '" + method.name() + "';\n" +
                "request.environment = " + environment + ";\n" +
                "request.environment.selectedEnv = '" + selectedEnv + "';\n" +
                "request.environment.get = function(name) {\n" +
                "    return this[name] !== undefined ? this[name] : null;\n" +
                "};\n" +
                "request.headers = " + headers + ";\n" +
                "request.globalVariables = " + globalVariables + ";\n" +
                "request.headers.all = function() { return headersAll(this); };\n" +
                "request.headers.set = function(name, value) { javaBridge.setHeader(name, value); };\n" +
                "request.headers.add = function(name, value) { javaBridge.addHeader(name, value); };\n" +
                "request.headers.findByName = function(name) { return headersFindByName(this, name); };";

        context.evaluateString(reqScriptableObject, js, "initRequestBody.js", 1, null);
    }

    public List<String> evalJsBeforeRequest(List<PreJsFile> preJsFiles, List<HttpScriptBody> jsListBeforeReq) {
        if (jsListBeforeReq.isEmpty() && preJsFiles.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            GlobalLog.setTabName(tabName);

            List<String> resList = new ArrayList<>();
            resList.add("/*\n" + NlsBundle.message("pre.desc") + ":\n");

            Map<Boolean, List<PreJsFile>> partitioned = new HashMap<>();
            List<PreJsFile> withUrl = new ArrayList<>();
            List<PreJsFile> withoutUrl = new ArrayList<>();
            for (PreJsFile file : preJsFiles) {
                if (file.getUrlFile() != null) {
                    withUrl.add(file);
                } else {
                    withoutUrl.add(file);
                }
            }

            executeNpmFiles(withUrl);

            for (PreJsFile file : withoutUrl) {
                String fileName = file.getFile().getName();
                evalJs(file.getContent(), 1, fileName, reqScriptableObject);
            }

            if (!jsListBeforeReq.isEmpty()) {
                var virtualFile = jsListBeforeReq.get(0).getContainingFile().getVirtualFile();
                var document = FileDocumentManager.getInstance().getDocument(virtualFile);

                for (HttpScriptBody scriptBody : jsListBeforeReq) {
                    int rowNum = document.getLineNumber(scriptBody.getTextOffset()) + 1;
                    evalJs(scriptBody.getText(), rowNum, virtualFile.getName(), reqScriptableObject);
                }
            }

            resList.add(GlobalLog.getAndClearLogs() + CR_LF);
            resList.add("*/\n");

            return resList;
        } catch (Exception e) {
            GlobalLog.clearLogs();
            throw e;
        }
    }

    private void executeNpmFiles(List<PreJsFile> npmFiles) {
        if (npmFiles.isEmpty()) {
            return;
        }

        List<ScriptableObject> libraryScriptableObjects = new ArrayList<>();

        for (PreJsFile file : npmFiles) {
            String fileName = file.getFile().getName();
            ScriptableObject scriptableObject = libraryLoadedMap.get(fileName);

            if (scriptableObject == null) {
                scriptableObject = (ScriptableObject) context.initStandardObjects();

                evalJs(file.getContent(), 1, fileName, scriptableObject);

                libraryLoadedMap.put(fileName, scriptableObject);

                System.out.println("Loaded and cached js library: " + fileName);
            }

            libraryScriptableObjects.add(scriptableObject);
        }

        Scriptable globalProto = reqScriptableObject.getPrototype();

        ScriptableObject previous = null;

        for (ScriptableObject obj : libraryScriptableObjects) {
            if (previous == null) {
                previous = obj;
                continue;
            }

            previous.setPrototype(obj);
            previous = obj;
        }

        reqScriptableObject.setPrototype(libraryScriptableObjects.get(0));

        libraryScriptableObjects.get(libraryScriptableObjects.size() - 1).setPrototype(globalProto);
    }

    public String evalJsAfterRequest(
            HttpScriptBody jsScript,
            Triple triple,
            int statusCode,
            Map<String, List<String>> headerMap
    ) {
        if (jsScript == null) {
            return null;
        }

        try {
            GlobalLog.setTabName(tabName);

            String headers = gson.toJson(headerMap);

            String js;
            SimpleTypeEnum typeEnum = (SimpleTypeEnum) triple.getFirst();
            switch (typeEnum) {
                case JSON:
                    byte[] bytes = (byte[]) triple.getSecond();
                    jsonStr = new String(bytes, StandardCharsets.UTF_8);

                    js = "var response = { body: " + jsonStr + " };\n" +
                            "response.body.jsonPath = {\n" +
                            "    evaluate: function(expression) { return javaBridge.evaluateJsonPath(expression); }\n" +
                            "};";
                    break;

                case XML:
                    bytes = (byte[]) triple.getSecond();
                    String xmlStr = new String(bytes, StandardCharsets.UTF_8);

                    try {
                        var documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        xmlDoc = documentBuilder.parse(new InputSource(new StringReader(xmlStr)));
                        xPath = xPathFactory.newXPath();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    js = "var response = { body: javaBridge.getXmlDoc() };\n" +
                            "response.body.xpath = {\n" +
                            "    evaluate: function(expression) { return javaBridge.evaluateXPath(expression); }\n" +
                            "};";
                    break;

                case TEXT:
                    bytes = (byte[]) triple.getSecond();
                    String bodyText = HttpUtils.convertToJsString(new String(bytes, StandardCharsets.UTF_8));

                    js = "var response = { body: " + bodyText + " };";
                    break;

                default:
                    bodyArray = (byte[]) triple.getSecond();

                    js = "var response = { body: javaBridge.getBodyArray() };";
                    break;
            }

            js += "response.status = " + statusCode + ";\n" +
                    "response.headers = " + headers + ";\n" +
                    "response.headers.all = function() { return headersAll(this); };\n" +
                    "response.headers.findByName = function(name) { return headersFindByName(this, name); };\n" +
                    "response.headers.valueOf = function(name) { return headersFindByName(this, name); };\n" +
                    "response.headers.valuesOf = function(name) { return headersFindListByName(this, name) || []; };\n" +
                    "response.contentType = resolveContentType(response.headers);";

            context.evaluateString(reqScriptableObject, js, "initResponseBody.js", 1, null);

            var virtualFile = jsScript.getContainingFile().getVirtualFile();
            var document = FileDocumentManager.getInstance().getDocument(virtualFile);
            int rowNum = document.getLineNumber(jsScript.getTextOffset()) + 1;

            try {
                evalJs(jsScript.getText(), rowNum, virtualFile.getName(), reqScriptableObject);
            } catch (Exception e) {
                GlobalLog.log(e.toString());
            }

            context.evaluateString(reqScriptableObject, "delete response;", "dummy.js", 1, null);

            return GlobalLog.getAndClearLogs();
        } catch (Exception e) {
            GlobalLog.clearLogs();
            throw e;
        }
    }

    private void evalJs(String jsStr, int rowNum, String fileName, ScriptableObject scriptableObject) {
        try {
            context.evaluateString(scriptableObject, jsStr, fileName, rowNum, null);

            jsGlobalVariableMap = getJsGlobalVariables();
        } catch (WrappedException e) {
            System.err.println("WrappedException");
            e.printStackTrace();

            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException || cause instanceof IllegalArgumentException ||
                    cause instanceof HttpFileException) {
                rethrowException(e.getStackTrace(), cause.toString(), fileName);
            }

            if (cause instanceof JsFileException) {
                throw (RuntimeException) cause.getCause();
            }

            throw new EvaluatorException(e.getWrappedException().toString(), fileName, e.lineNumber());
        } catch (JavaScriptException e) {
            System.err.println("JavaScriptException");
            e.printStackTrace();

            rethrowException(e.getStackTrace(), e.toString(), fileName);
        } catch (Exception e) {
            System.err.println("Exception");
            e.printStackTrace();

            throw e;
        }
    }

    private void rethrowException(StackTraceElement[] stackTraces, String message, String fileName) {
        for (StackTraceElement stackTraceElement : stackTraces) {
            if (!fileName.equals(stackTraceElement.getFileName())) {
                continue;
            }

            throw new EvaluatorException(message, fileName, stackTraceElement.getLineNumber());
        }
    }

    public String getRequestVariable(String key) {
        if (!ScriptableObject.hasProperty(reqScriptableObject, "request")) {
            return null;
        }

        boolean hasKey = (Boolean) ScriptableObject.callMethod(reqScriptableObject, "hasRequestVariableKey", new Object[]{key});
        if (!hasKey) {
            return null;
        }

        Object res = ScriptableObject.callMethod(reqScriptableObject, "getRequestVariable", new Object[]{key});
        return res != null ? res.toString() : "null";
    }

    public String getJsGlobalVariable(String key) {
        boolean hasKey = (Boolean) ScriptableObject.callMethod(reqScriptableObject, "hasGlobalVariableKey", new Object[]{key});
        if (!hasKey) {
            return null;
        }

        Object res = ScriptableObject.callMethod(reqScriptableObject, "getGlobalVariable", new Object[]{key});
        return res != null ? res.toString() : "null";
    }

    public Map<String, String> getJsGlobalVariables() {
        NativeObject dataHolder = (NativeObject) context.evaluateString(
                reqScriptableObject, "client.global.dataHolder", "dummy.js", 1, null);

        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : dataHolder.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : "null");
        }

        return map;
    }

    public LinkedMultiValueMap<String, String> getHeaderMap() {
        return originalJavaBridge.getHeaderMap();
    }

    public static void setGlobalVariable(String key, String value) {
        String js = "client.global.dataHolder['" + key + "'] = '" + value + "';";
        context.evaluateString(global, js, "dummy.js", 1, null);
    }

    // Helper class for triple
    public static class Triple {
        private final Object first;
        private final Object second;
        private final Object third;

        public Triple(Object first, Object second, Object third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public Object getFirst() {
            return first;
        }

        public Object getSecond() {
            return second;
        }

        public Object getThird() {
            return third;
        }
    }
}
