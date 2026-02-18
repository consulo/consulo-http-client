package consulo.restClient.impl.java.spring;

import org.javamaster.httpclient.model.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public enum SpringHttpMethod {
    REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", HttpMethod.REQUEST),
    GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", HttpMethod.GET),
    POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", HttpMethod.POST),
    PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", HttpMethod.PUT),
    DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", HttpMethod.DELETE),
    PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", HttpMethod.PATCH),
    REQUEST_PARAM("org.springframework.web.bind.annotation.RequestParam", HttpMethod.UNKNOWN),
    REQUEST_BODY("org.springframework.web.bind.annotation.RequestBody", HttpMethod.UNKNOWN),
    PATH_VARIABLE("org.springframework.web.bind.annotation.PathVariable", HttpMethod.UNKNOWN),
    REQUEST_HEADER("org.springframework.web.bind.annotation.RequestHeader", HttpMethod.UNKNOWN);

    private final String qualifiedName;
    private final HttpMethod method;
    private final String shortName;

    SpringHttpMethod(String qualifiedName, HttpMethod method) {
        this.qualifiedName = qualifiedName;
        this.method = method;
        this.shortName = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getShortName() {
        return shortName;
    }

    private static final Map<String, SpringHttpMethod> map;
    private static final Map<String, SpringHttpMethod> shortMap;

    static {
        map = new HashMap<>();
        shortMap = new HashMap<>();
        for (SpringHttpMethod it : values()) {
            map.put(it.qualifiedName, it);
            shortMap.put(it.shortName, it);
        }
    }

    public static SpringHttpMethod getByQualifiedName(String qualifiedName) {
        return map.get(qualifiedName);
    }

    public static SpringHttpMethod getByShortName(String requestMapping) {
        return shortMap.get(requestMapping);
    }
}
