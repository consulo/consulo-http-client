package consulo.restClient.impl.java;

import org.javamaster.httpclient.model.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRefAnnotation {
    private final List<String> paths = new ArrayList<>();
    private final List<HttpMethod> methods = new ArrayList<>();

    public void addPath(String... paths) {
        this.paths.addAll(Arrays.asList(paths));
    }

    public void addMethods(HttpMethod... methods) {
        this.methods.addAll(Arrays.asList(methods));
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }
}
