package consulo.httpClient.impl.java;

public enum Control {
    Controller("Controller", "org.springframework.stereotype.Controller"),
    RestController("RestController", "org.springframework.web.bind.annotation.RestController");

    private final String simpleName;
    private final String qualifiedName;

    Control(String simpleName, String qualifiedName) {
        this.simpleName = simpleName;
        this.qualifiedName = qualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}
