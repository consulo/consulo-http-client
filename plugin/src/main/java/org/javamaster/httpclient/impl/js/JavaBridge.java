package org.javamaster.httpclient.impl.js;

import com.jayway.jsonpath.JsonPath;
import consulo.virtualFileSystem.VirtualFileManager;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.annos.JsBridge;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;
import org.javamaster.httpclient.impl.exception.HttpFileException;
import org.javamaster.httpclient.impl.exception.JsFileException;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.VirtualFileUtils;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.LinkedList;

/**
 * JavaScript bridge for HTTP client
 *
 * @author yudong
 */
@SuppressWarnings("unused")
public class JavaBridge {
    private final JsExecutor jsExecutor;
    private final String parentPath;
    private final LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();

    public JavaBridge(JsExecutor jsExecutor) {
        this.jsExecutor = jsExecutor;
        this.parentPath = jsExecutor.getHttpFile().getVirtualFile().getParent().getPath();
    }

    public LinkedMultiValueMap<String, String> getHeaderMap() {
        return headerMap;
    }

    @JsBridge(jsFun = "require(path)")
    public ScriptableObject require(String path) throws Exception {
        ScriptableObject scriptableObject = jsExecutor.getReqScriptableObject();

        String filePath = HttpUtils.constructFilePath(path, parentPath);
        File file = new File(filePath);

        String jsStr = VirtualFileUtils.readNewestContent(file);

        try {
            JsExecutor.getContext().evaluateString(scriptableObject, jsStr, file.getName(), 1, null);
        }
        catch (Exception e) {
            throw new JsFileException(e.toString(), e);
        }

        return scriptableObject;
    }

    @JsBridge(jsFun = "readString(path)")
    public String readString(String path) throws Exception {
        String filePath = HttpUtils.constructFilePath(path, parentPath);
        File file = new File(filePath);

        return VirtualFileUtils.readNewestContent(file);
    }

    @JsBridge(jsFun = "getBodyArray()")
    public Object getBodyArray() {
        return Context.javaToJS(jsExecutor.getBodyArray(), jsExecutor.getReqScriptableObject());
    }

    @JsBridge(jsFun = "getBodyString()")
    public String getBodyString() {
        byte[] bodyArray = jsExecutor.getBodyArray();
        return bodyArray != null ? new String(bodyArray, StandardCharsets.UTF_8) : "";
    }

    @JsBridge(jsFun = "convertBodyToByteArray(str)")
    public Object convertBodyToByteArray(String str) {
        if ("null".equals(str)) {
            return null;
        }

        return Context.javaToJS(str.getBytes(StandardCharsets.UTF_8), jsExecutor.getReqScriptableObject());
    }

    @JsBridge(jsFun = "getXmlDoc()")
    public Object getXmlDoc() {
        NativeJavaObject resObj = (NativeJavaObject) Context.javaToJS(jsExecutor.getXmlDoc(), jsExecutor.getReqScriptableObject());
        resObj.setPrototype(JsExecutor.getContext().initStandardObjects());
        return resObj;
    }

    @JsBridge(jsFun = "evaluateXPath(expression)")
    public Object evaluateXPath(String expression) throws Exception {
        try {
            return jsExecutor.getXPath().evaluate(expression, jsExecutor.getXmlDoc());
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "evaluateJsonPath(expression)")
    public Object evaluateJsonPath(String expression) throws Exception {
        try {
            return JsonPath.read(jsExecutor.getJsonStr(), expression);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "xpath(obj, expression)")
    public Object xpath(Object obj, String expression) throws Exception {
        try {
            return JsExecutor.getXPathFactory().newXPath().evaluate(expression, obj);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "jsonPath(obj, expression)")
    public Object jsonPath(Object obj, String expression) throws Exception {
        try {
            return JsonPath.read(obj, expression);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "btoa(bytes)")
    public String btoa(String bytes) throws Exception {
        try {
            return Base64.getEncoder().encodeToString(bytes.getBytes());
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "atob(str)")
    public String atob(String str) throws Exception {
        try {
            return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "base64ToFile(base64, path)")
    public void base64ToFile(String base64, String path) throws Exception {
        try {
            String tmpPath = VariableResolver.resolveInnerVariable(path, parentPath, jsExecutor.getProject());

            String filePath = HttpUtils.constructFilePath(tmpPath, parentPath);

            File file = new File(filePath);
            File parentFile = file.getParentFile();

            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            else {
                if (file.exists()) {
                    file.delete();
                }
            }

            byte[] bytes = Base64.getDecoder().decode(base64);

            Files.write(file.toPath(), bytes, StandardOpenOption.CREATE);

            GlobalLog.log(NlsBundle.message("base64.convert.to.file") + " " + file.getCanonicalPath());

            VirtualFileManager.getInstance().asyncRefresh(null);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    @JsBridge(jsFun = "setHeader(name, value)")
    public void setHeader(String name, String value) {
        LinkedList<String> list = new LinkedList<>();
        String str = value != null ? value : "";
        list.add(str);

        headerMap.put(name, list);

        GlobalLog.log(NlsBundle.message("req.header.set", name, str));
    }

    @JsBridge(jsFun = "addHeader(name, value)")
    public void addHeader(String name, String value) {
        String str = value != null ? value : "";

        headerMap.add(name, str);

        GlobalLog.log(NlsBundle.message("req.header.add", name, str));
    }

    @JsBridge(jsFun = "callJava(methodName, arg0, arg1)")
    public String callJava(String methodName, Object arg0, Object arg1) throws Exception {
        try {
            Object[] args = new Object[2];
            int argIndex = 0;

            Object convertedArg = convertArg(arg0);
            if (convertedArg != null) {
                args[argIndex++] = convertedArg;
            }

            convertedArg = convertArg(arg1);
            if (convertedArg != null) {
                args[argIndex++] = convertedArg;
            }

            Object[] finalArgs = new Object[argIndex];
            System.arraycopy(args, 0, finalArgs, 0, argIndex);

            InnerVariableEnum variableEnum = InnerVariableEnum.getEnum(methodName);
            if (variableEnum == null) {
                throw new IllegalArgumentException(NlsBundle.message("method.not.exists", methodName));
            }

            return variableEnum.exec("", finalArgs);
        }
        catch (Exception e) {
            throw new HttpFileException(e.toString(), e);
        }
    }

    private Object convertArg(Object arg) {
        if ("undefined".equals(arg)) {
            return null;
        }
        if (arg instanceof Double) {
            return ((Double) arg).intValue();
        }

        return arg;
    }
}
