package consulo.httpClient.script.rhino;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.javamaster.httpclient.impl.js.HttpScriptTestResult;
import org.javamaster.httpclient.impl.js.JsExecutor;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.util.lang.StringUtil;
import consulo.util.lang.Trinity;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpReqInfo;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpScriptBody;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;
import org.mozilla.javascript.*;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.UnaryOperator;

import static org.javamaster.httpclient.utils.HttpUtilsPart.CR_LF;

/**
 * Handler scripts of one request run. Java is exposed to scripts only through {@link LambdaFunction}s named
 * {@code __bridge_*}, see bootstrap.js.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
public class RhinoJsExecutor implements JsExecutor {
    private static final String BOOTSTRAP = loadResource("bootstrap.js");

    /**
     * crypto-js compiled once, run on first {@code CryptoJS} access of a script.
     */
    private static volatile @Nullable Script ourCryptoJsScript;
    private static final String EXIT_SIGNAL = "__http_client_exit__";

    private final HttpFile myHttpFile;
    private final Map<String, String> myGlobalVariables;
    private final Map<String, String> myRequestVariables = new LinkedHashMap<>();
    private final LinkedMultiValueMap<String, String> myScriptHeaders = new LinkedMultiValueMap<>();
    private final List<String> myLogs = new ArrayList<>();
    private final List<HttpScriptTestResult> myTests = new ArrayList<>();

    private Map<String, String> myEnvironment = Map.of();
    private LinkedMultiValueMap<String, String> myRequestHeaders = new LinkedMultiValueMap<>();
    private String myMethod = "";
    private String myRawUrl = "";
    private UnaryOperator<String> myUrlSubstitutor = UnaryOperator.identity();
    private @Nullable String myRequestBody;

    private @Nullable ScriptableObject myScope;

    public RhinoJsExecutor(HttpFile httpFile, Map<String, String> globalVariables) {
        myHttpFile = httpFile;
        myGlobalVariables = globalVariables;
    }

    @Override
    public @Nullable String getRequestVariable(String key) {
        return myRequestVariables.get(key);
    }

    @Override
    public @Nullable String getJsGlobalVariable(String key) {
        return myGlobalVariables.get(key);
    }

    @Override
    public Map<String, String> getJsGlobalVariables() {
        synchronized (myGlobalVariables) {
            return new LinkedHashMap<>(myGlobalVariables);
        }
    }

    @Override
    public void initJsRequestObj(HttpReqInfo reqInfo,
                                 HttpRequestEnum method,
                                 LinkedMultiValueMap<String, String> reqHeaderMap,
                                 @Nullable String selectedEnv,
                                 Map<String, String> fileScopeVariableMap) {
        myMethod = method.name();
        myRequestHeaders = reqHeaderMap;
        myRequestBody = reqInfo.getReqBody() instanceof String text ? text : null;

        Map<String, String> environment = HttpUtilsPart.gson.fromJson(reqInfo.getEnvironment(),
            new TypeToken<Map<String, String>>() {
            }.getType());
        myEnvironment = environment == null ? Map.of() : environment;
    }

    @Override
    public void initRequestUrl(String rawUrl, UnaryOperator<String> substitutor) {
        myRawUrl = rawUrl;
        myUrlSubstitutor = substitutor;
    }

    @Override
    public List<String> evalJsBeforeRequest(List<PreJsFile> preJsFiles, List<HttpScriptBody> jsListBeforeReq) {
        if (preJsFiles.isEmpty() && jsListBeforeReq.isEmpty()) {
            return List.of();
        }

        myLogs.clear();
        for (PreJsFile preJsFile : preJsFiles) {
            String content = preJsFile.getContent();
            if (content != null && preJsFile.getFile() != null) {
                eval(content, preJsFile.getFile().getName(), 1);
            }
        }
        for (HttpScriptBody scriptBody : jsListBeforeReq) {
            eval(scriptBody.getText(), myHttpFile.getName(), lineOf(scriptBody));
        }

        List<String> result = new ArrayList<>();
        result.add("/*" + CR_LF + HttpClientLocalize.preDesc().get() + ":" + CR_LF);
        result.add(String.join(CR_LF, myLogs) + CR_LF);
        result.add("*/" + CR_LF);
        return result;
    }

    @Override
    public LinkedMultiValueMap<String, String> getHeaderMap() {
        return myScriptHeaders;
    }

    @Override
    public @Nullable String evalJsAfterRequest(@Nullable HttpScriptBody jsScript,
                                               Trinity<SimpleTypeEnum, byte[], String> triple,
                                               int statusCode,
                                               Map<String, List<String>> headerMap) {
        myTests.clear();
        if (jsScript == null) {
            return null;
        }

        myLogs.clear();

        SimpleTypeEnum type = triple.getFirst();
        String contentType = triple.getThird();
        String mimeType = contentType == null ? "" : contentType.split(";")[0].trim();
        String charset = charsetOf(contentType);
        String bodyText = type.isBinary() ? "" : new String(triple.getSecond(), StandardCharsets.UTF_8);

        JsonArray headers = new JsonArray();
        for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            for (String value : entry.getValue()) {
                JsonObject header = new JsonObject();
                header.addProperty("name", entry.getKey());
                header.addProperty("value", value);
                headers.add(header);
            }
        }

        ScriptableObject scope = getScope();
        RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> {
            Function createResponse = (Function) ScriptableObject.getProperty(scope, "__createResponse");
            Object response = createResponse.call(cx, scope, scope, new Object[]{
                statusCode,
                headers.toString(),
                bodyText,
                type == SimpleTypeEnum.JSON,
                mimeType,
                charset
            });
            ScriptableObject.putProperty(scope, "response", response);
            return null;
        });

        try {
            eval(jsScript.getText(), myHttpFile.getName(), lineOf(jsScript));
        }
        finally {
            RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> ScriptableObject.deleteProperty(scope, "response"));
        }

        for (HttpScriptTestResult test : myTests) {
            myLogs.add((test.passed() ? "[PASSED] " : "[FAILED] ") + test.name() + (test.message() == null ? "" : ": " + test.message()));
        }
        return String.join(CR_LF, myLogs);
    }

    @Override
    public List<HttpScriptTestResult> getTestResults() {
        return List.copyOf(myTests);
    }

    private static String charsetOf(@Nullable String contentType) {
        if (contentType != null) {
            for (String part : contentType.split(";")) {
                String trimmed = part.trim();
                if (trimmed.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                    return trimmed.substring("charset=".length());
                }
            }
        }
        return "";
    }

    private int lineOf(HttpScriptBody scriptBody) {
        return StringUtil.offsetToLineNumber(myHttpFile.getText(), scriptBody.getTextOffset()) + 1;
    }

    private void eval(String script, String fileName, int line) {
        ScriptableObject scope = getScope();
        try {
            RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> cx.evaluateString(scope, script, fileName, line, null));
        }
        catch (JavaScriptException e) {
            if (EXIT_SIGNAL.equals(e.getValue())) {
                return;
            }
            throw new IllegalStateException(fileName + ":" + e.lineNumber() + ": " + e.details(), e);
        }
        catch (RhinoException e) {
            throw new IllegalStateException(fileName + ":" + e.lineNumber() + ": " + e.details(), e);
        }
    }

    private ScriptableObject getScope() {
        if (myScope != null) {
            return myScope;
        }

        myScope = RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> {
            ScriptableObject scope = cx.initStandardObjects();
            defineBridge(scope);
            cx.evaluateString(scope, BOOTSTRAP, "bootstrap.js", 1, null);
            return scope;
        });
        return myScope;
    }

    private void defineBridge(ScriptableObject scope) {
        function(scope, "__bridge_log", 1, args -> {
            myLogs.add(string(args, 0));
            return Undefined.instance;
        });
        function(scope, "__bridge_test", 3, args -> {
            String name = string(args, 0);
            boolean passed = Context.toBoolean(args[1]);
            myTests.add(passed ? HttpScriptTestResult.passed(name) : HttpScriptTestResult.failed(name, String.valueOf(nullableString(args, 2))));
            return Undefined.instance;
        });
        function(scope, "__bridge_varGet", 2, args -> variables(string(args, 0)).get(string(args, 1)));
        function(scope, "__bridge_varSet", 3, args -> {
            Map<String, String> variables = variables(string(args, 0));
            String value = nullableString(args, 2);
            if (value == null) {
                variables.remove(string(args, 1));
            }
            else {
                variables.put(string(args, 1), value);
            }
            return Undefined.instance;
        });
        function(scope, "__bridge_varIsEmpty", 1, args -> variables(string(args, 0)).isEmpty());
        function(scope, "__bridge_varClearAll", 1, args -> {
            variables(string(args, 0)).clear();
            return Undefined.instance;
        });
        function(scope, "__bridge_envGet", 1, args -> myEnvironment.get(string(args, 0)));
        function(scope, "__bridge_requestMethod", 0, args -> myMethod);
        function(scope, "__bridge_requestBody", 0, args -> myRequestBody);
        function(scope, "__bridge_requestUrl", 1, args -> args.length > 0 && Context.toBoolean(args[0])
            ? myUrlSubstitutor.apply(myRawUrl)
            : myRawUrl);
        function(scope, "__bridge_readFile", 1, args -> {
            try {
                return Files.readString(resolvePath(string(args, 0)), StandardCharsets.UTF_8);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        function(scope, "__bridge_btoa", 1, args -> Base64.getEncoder().encodeToString(string(args, 0).getBytes(StandardCharsets.UTF_8)));
        function(scope, "__bridge_atob", 1, args -> new String(Base64.getDecoder().decode(string(args, 0)), StandardCharsets.UTF_8));
        function(scope, "__bridge_base64ToFile", 2, args -> {
            Path file = resolvePath(string(args, 1));
            try {
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.write(file, Base64.getDecoder().decode(string(args, 0)));
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return file.toString();
        });
        function(scope, "__bridge_xpath", 2, args -> evaluateXPath(string(args, 0), string(args, 1)));
        function(scope, "__bridge_cryptoJs", 0, args -> loadCryptoJs());
        function(scope, "__bridge_requestHeaders", 0, args -> {
            JsonArray headers = new JsonArray();
            addHeaders(headers, myRequestHeaders);
            addHeaders(headers, myScriptHeaders);
            return headers.toString();
        });
        function(scope, "__bridge_requestHeaderSet", 2, args -> {
            String name = string(args, 0);
            myScriptHeaders.remove(name);
            myRequestHeaders.remove(name);
            myScriptHeaders.add(name, string(args, 1));
            return Undefined.instance;
        });
        function(scope, "__bridge_requestHeaderAdd", 2, args -> {
            myScriptHeaders.add(string(args, 0), string(args, 1));
            return Undefined.instance;
        });
    }

    private Path resolvePath(String path) {
        Path file = Path.of(path);
        if (file.isAbsolute()) {
            return file;
        }
        return Path.of(myHttpFile.getVirtualFile().getParent().getPath()).resolve(file).normalize();
    }

    private static String evaluateXPath(String xml, String expression) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return XPathFactory.newInstance().newXPath().evaluate(expression, document);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("XPath '" + expression + "' failed: " + e.getMessage(), e);
        }
    }

    private static Object loadCryptoJs() {
        Script script = ourCryptoJsScript;
        if (script == null) {
            script = RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> cx.compileString(loadResource("crypto-js.js"), "crypto-js.js", 1, null));
            ourCryptoJsScript = script;
        }
        Script compiled = script;
        return RhinoJsExecutorFactory.CONTEXT_FACTORY.call(cx -> {
            ScriptableObject cryptoScope = cx.initStandardObjects();
            compiled.exec(cx, cryptoScope);
            return ScriptableObject.getProperty(cryptoScope, "CryptoJS");
        });
    }

    private Map<String, String> variables(String kind) {
        return "global".equals(kind) ? myGlobalVariables : myRequestVariables;
    }

    private static void addHeaders(JsonArray target, LinkedMultiValueMap<String, String> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                JsonObject header = new JsonObject();
                header.addProperty("name", entry.getKey());
                header.addProperty("value", value);
                target.add(header);
            }
        }
    }

    @FunctionalInterface
    private interface Bridge {
        @Nullable Object call(Object[] args);
    }

    private static void function(ScriptableObject scope, String name, int arity, Bridge bridge) {
        LambdaFunction function = new LambdaFunction(scope, name, arity, (cx, callScope, thisObj, args) -> bridge.call(args));
        ScriptableObject.defineProperty(scope, name, function, ScriptableObject.DONTENUM);
    }

    private static String string(Object[] args, int index) {
        String value = nullableString(args, index);
        return value == null ? "" : value;
    }

    private static @Nullable String nullableString(Object[] args, int index) {
        if (index >= args.length || args[index] == null || args[index] instanceof Undefined) {
            return null;
        }
        return Context.toString(args[index]);
    }

    private static String loadResource(String name) {
        try (InputStream stream = RhinoJsExecutor.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalStateException(name + " not found");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
