package consulo.restClient.impl.java.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.intellij.java.language.psi.*;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.Pair;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.utils.HttpUtils;
import org.javamaster.httpclient.impl.utils.PsiUtils;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.ParamEnum;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author yudong
 */
public class DubboRequest implements DubboHandler {
    private static final ApplicationConfig application;

    static {
        application = new ApplicationConfig();
        application.setName("HttpClient");
        application.setQosEnable(false);
    }

    private final String tabName;
    private final String url;
    private final LinkedMultiValueMap<String, String> reqHeaderMap;
    private final Object reqBodyStr;
    private final List<String> httpReqDescList;
    private final Map<String, String> paramMap;

    private final String methodName;
    private final String interfaceCls;
    private final String interfaceName;
    private final String version;
    private final String registry;
    private final LinkedHashMap<?, ?> reqBodyMap;

    private final String targetInterfaceName;
    private final String[] paramTypeNameArray;
    private final Object[] paramValueArray;

    public DubboRequest(
            String tabName,
            String url,
            LinkedMultiValueMap<String, String> reqHeaderMap,
            Object reqBodyStr,
            List<String> httpReqDescList,
            Module module,
            Project project,
            Map<String, String> paramMap
    ) {
        this.tabName = tabName;
        this.url = url;
        this.reqHeaderMap = reqHeaderMap;
        this.reqBodyStr = reqBodyStr;
        this.httpReqDescList = httpReqDescList;
        this.paramMap = paramMap;

        // Initialize lazy properties
        List<String> methodValues = reqHeaderMap.get(DubboUtils.METHOD_KEY);
        if (methodValues == null) {
            throw new IllegalArgumentException(NlsBundle.message("missing.header"));
        }
        this.methodName = methodValues.get(0);

        List<String> interfaceClsValues = reqHeaderMap.get(DubboUtils.INTERFACE_KEY);
        this.interfaceCls = interfaceClsValues != null ? interfaceClsValues.get(0) : null;

        List<String> interfaceNameValues = reqHeaderMap.get(DubboUtils.INTERFACE_NAME);
        this.interfaceName = interfaceNameValues != null ? interfaceNameValues.get(0) : null;

        List<String> versionValues = reqHeaderMap.get(DubboUtils.VERSION);
        this.version = versionValues != null ? versionValues.get(0) : null;

        List<String> registryValues = reqHeaderMap.get(DubboUtils.REGISTRY);
        this.registry = registryValues != null ? registryValues.get(0) : null;

        if (reqBodyStr != null) {
            this.reqBodyMap = HttpUtils.gson.fromJson((String) reqBodyStr, LinkedHashMap.class);
        } else {
            this.reqBodyMap = null;
        }

        // Initialize based on interfaceCls or interfaceName
        if (interfaceCls != null) {
            targetInterfaceName = interfaceCls;
            PsiClass psiClass;
            if (module != null) {
                psiClass = DubboUtils.findInterface(module, targetInterfaceName);
                if (psiClass == null) {
                    throw new IllegalArgumentException(
                            NlsBundle.message("interface.not.resolved", targetInterfaceName, module.getName())
                    );
                }
            } else {
                psiClass = DubboUtils.findInterface(project, targetInterfaceName);
                if (psiClass == null) {
                    throw new IllegalArgumentException(
                            NlsBundle.message("interface.not.resolved1", targetInterfaceName, project.getName())
                    );
                }
            }

            PsiMethod targetMethod = findTargetMethod(psiClass, reqBodyMap);

            PsiParameter[] parameters = targetMethod.getParameterList().getParameters();
            paramTypeNameArray = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                PsiParameter param = parameters[i];
                PsiType type = param.getType();
                PsiClass psiType = PsiUtils.resolvePsiType(type);
                if (psiType != null) {
                    String qualifiedName = psiType.getQualifiedName();
                    if (qualifiedName != null) {
                        paramTypeNameArray[i] = qualifiedName;
                        continue;
                    }
                }

                if (type instanceof PsiPrimitiveType) {
                    paramTypeNameArray[i] = type.getPresentableText();
                    continue;
                }

                throw new IllegalArgumentException(NlsBundle.nls("param.not.resolved", param.getName()));
            }

            paramValueArray = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                String name = parameters[i].getName();
                paramValueArray[i] = reqBodyMap.get(name);
            }
        } else {
            if (interfaceName == null) {
                throw new IllegalArgumentException(
                        NlsBundle.message("dubbo.all.blank", DubboUtils.INTERFACE_KEY, DubboUtils.INTERFACE_NAME)
                );
            }

            targetInterfaceName = interfaceName;
            if (reqBodyMap == null) {
                paramTypeNameArray = new String[0];
                paramValueArray = new Object[0];
            } else {
                List<String> tmpTypeList = new ArrayList<>();
                List<Object> tmpValueList = new ArrayList<>();

                for (Map.Entry<?, ?> entry : reqBodyMap.entrySet()) {
                    String paramName = String.valueOf(entry.getKey());
                    List<String> argTypes = reqHeaderMap.get(paramName);
                    if (argTypes == null) {
                        throw new IllegalArgumentException(NlsBundle.message("dubbo.miss.header", paramName));
                    }
                    tmpTypeList.add(argTypes.get(0));
                    tmpValueList.add(entry.getValue());
                }

                paramTypeNameArray = tmpTypeList.toArray(new String[0]);
                paramValueArray = tmpValueList.toArray(new Object[0]);
            }
        }
    }

    @Override
    public CompletableFuture<Pair<byte[], Long>> sendAsync() {
        httpReqDescList.add("/*" + HttpUtils.CR_LF);
        httpReqDescList.add(NlsBundle.message("call.dubbo.name", methodName) + HttpUtils.CR_LF);
        httpReqDescList.add(
                NlsBundle.message("call.dubbo.param.typeNames", arrayToString(paramTypeNameArray)) + HttpUtils.CR_LF
        );
        httpReqDescList.add(
                NlsBundle.message("call.dubbo.params", arrayToString(paramValueArray)) + HttpUtils.CR_LF
        );
        httpReqDescList.add("*/" + HttpUtils.CR_LF);

        String commentTabName = "### " + tabName + HttpUtils.CR_LF;
        httpReqDescList.add(commentTabName);
        httpReqDescList.add("DUBBO " + url + HttpUtils.CR_LF);

        reqHeaderMap.forEach((name, values) -> {
            for (String value : values) {
                httpReqDescList.add(name + ": " + value + HttpUtils.CR_LF);
            }
        });

        httpReqDescList.add(HttpUtils.CR_LF);

        if (reqBodyMap != null) {
            httpReqDescList.add((String) reqBodyStr);
        }

        return CompletableFuture.supplyAsync(() -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

                ReferenceConfig<GenericService> referenceConfig = createReferenceConfig();

                GenericService genericService = referenceConfig.get();

                long start = System.currentTimeMillis();
                Object result;
                try {
                    result = genericService.$invoke(methodName, paramTypeNameArray, paramValueArray);
                } finally {
                    referenceConfig.destroy();
                }
                long consumeTimes = System.currentTimeMillis() - start;

                String resJsonStr = HttpUtils.gson.toJson(result);

                byte[] byteArray = resJsonStr.getBytes(StandardCharsets.UTF_8);

                return new Pair<>(byteArray, consumeTimes);
            } finally {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        });
    }

    private ReferenceConfig<GenericService> createReferenceConfig() {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setGeneric(true);
        reference.setApplication(application);
        reference.setInterface(targetInterfaceName);
        String timeoutStr = paramMap.get(ParamEnum.TIMEOUT_NAME.getParam());
        int timeout = timeoutStr != null ? Integer.parseInt(timeoutStr) : HttpUtils.TIMEOUT;
        reference.setTimeout(timeout);
        reference.setRetries(1);
        reference.setCheck(false);

        if (version != null && !version.isBlank()) {
            reference.setVersion(version);
        }

        if (registry == null || registry.isBlank()) {
            reference.setUrl(url);
        } else {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registry);
            registryConfig.setTimeout(timeout);
            registryConfig.setRegister(false);

            reference.setRegistry(registryConfig);
        }

        return reference;
    }

    private PsiMethod findTargetMethod(PsiClass psiClass, LinkedHashMap<?, ?> reqMap) {
        PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
        if (methods.length == 0) {
            throw new IllegalArgumentException(NlsBundle.message("method.not.exists", methodName));
        }

        PsiMethod method;
        if (reqMap == null) {
            method = methods[0];
        } else {
            java.util.Set<?> paramNames = reqMap.keySet();
            method = null;
            for (PsiMethod m : methods) {
                java.util.Iterator<?> iterator = paramNames.iterator();
                PsiParameter[] parameterList = m.getParameterList().getParameters();
                boolean match = true;
                for (int i = 0; i < parameterList.length; i++) {
                    if (!iterator.hasNext()) {
                        match = false;
                        break;
                    }
                    String name = parameterList[i].getName();
                    Object jsonName = iterator.next();
                    if (!name.equals(jsonName)) {
                        match = false;
                        break;
                    }
                }
                if (match && !iterator.hasNext()) {
                    method = m;
                    break;
                }
            }

            if (method == null) {
                throw new IllegalArgumentException(NlsBundle.message("method.not.found", paramNames, methodName));
            }
        }

        return method;
    }

    private String arrayToString(Object[] array) {
        if (array == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
