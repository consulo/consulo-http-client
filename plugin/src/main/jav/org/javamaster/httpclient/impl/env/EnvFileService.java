package org.javamaster.httpclient.impl.env;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.WriteAction;
import consulo.fileEditor.FileEditorManager;
import consulo.json.JsonElementTypes;
import consulo.json.psi.*;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.inject.Inject;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;
import org.javamaster.httpclient.impl.factory.JsonPsiFactory;
import org.javamaster.httpclient.impl.psi.impl.TextVariableLazyFileElement;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.psi.HttpPsiUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.javamaster.httpclient.impl.index.HttpEnvironmentIndex.INDEX_ID;
import static org.javamaster.httpclient.impl.resolve.VariableResolver.VARIABLE_PATTERN;
import static org.javamaster.httpclient.impl.resolve.VariableResolver.escapeRegexp;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public final class EnvFileService {
    public static final String ENV_FILE_NAME = "http-client.env.json";
    public static final String PRIVATE_ENV_FILE_NAME = "http-client.private.env.json";
    public static final Set<String> ENV_FILE_NAMES = Set.of(ENV_FILE_NAME, PRIVATE_ENV_FILE_NAME);
    public static final String COMMON_ENV_NAME = "common";

    private final Project project;

    @Inject
    public EnvFileService(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Set<String> getPresetEnvSet(String httpFileParentPath) {
        Set<String> envSet = new LinkedHashSet<>();

        JsonFile jsonFile = getEnvJsonFile(PRIVATE_ENV_FILE_NAME, httpFileParentPath, project);
        List<String> privateEnvList = collectEnvNames(jsonFile);
        envSet.addAll(privateEnvList);

        JsonFile jsonPrivateFile = getEnvJsonFile(ENV_FILE_NAME, httpFileParentPath, project);
        List<String> envList = collectEnvNames(jsonPrivateFile);
        envSet.addAll(envList);

        envSet.remove(COMMON_ENV_NAME);

        return envSet;
    }

    private List<String> collectEnvNames(JsonFile jsonFile) {
        if (jsonFile == null) {
            return Collections.emptyList();
        }

        JsonValue jsonValue = jsonFile.getTopLevelValue();

        if (!(jsonValue instanceof JsonObject)) {
            System.err.println("The environment file: " + jsonFile.getVirtualFile().getPath() + " format does not conform to the specification!");
            return Collections.emptyList();
        }

        return ((JsonObject) jsonValue).getPropertyList().stream()
            .map(JsonProperty::getName)
            .collect(Collectors.toList());
    }

    public String getEnvValue(String key, String selectedEnv, String httpFileParentPath) {
        String envValue = getEnvValue(key, selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME);
        if (envValue != null) {
            return envValue;
        }

        envValue = getEnvValue(key, selectedEnv, httpFileParentPath, ENV_FILE_NAME);
        if (envValue != null) {
            return envValue;
        }

        envValue = getEnvValue(key, COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME);
        if (envValue != null) {
            return envValue;
        }

        return getEnvValue(key, COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME);
    }

    public void createEnvValue(String key, String selectedEnv, String httpFileParentPath, String envFileName) {
        JsonFile jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project);
        if (jsonFile == null) {
            return;
        }

        JsonValue topLevelValue = jsonFile.getTopLevelValue();
        if (!(topLevelValue instanceof JsonObject)) {
            return;
        }

        JsonProperty envProperty = ((JsonObject) topLevelValue).findProperty(selectedEnv);
        if (envProperty == null) {
            return;
        }

        JsonValue value = envProperty.getValue();
        if (!(value instanceof JsonObject)) {
            return;
        }

        JsonProperty newProperty = JsonPsiFactory.createStringProperty(project, key, "");
        JsonElement newComma = HttpPsiUtils.getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false);

        List<JsonProperty> propertyList = ((JsonObject) value).getPropertyList();

        if (!propertyList.isEmpty()) {
            value.addAfter(newComma, propertyList.get(propertyList.size() - 1));
        }

        JsonElement elementCopy = (JsonElement) value.addBefore(newProperty, value.getLastChild());

        // Move cursor to inside quotes
        ((Navigatable) elementCopy.getLastChild()).navigate(true);
        var caretModel = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (caretModel != null && caretModel.getCaretModel() != null) {
            caretModel.getCaretModel().moveToOffset(caretModel.getCaretModel().getOffset() + 1);
        }
    }

    private String getEnvValue(String key, String selectedEnv, String httpFileParentPath, String envFileName) {
        JsonLiteral literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, envFileName, project);
        if (literal == null) {
            return null;
        }

        String value = getJsonLiteralValue(literal);
        return resolveValue(value, httpFileParentPath);
    }

    public static VirtualFile createEnvFile(String name, boolean isPrivate, Project project) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        var selectedEditor = editorManager.getSelectedEditor();
        if (selectedEditor == null) {
            return null;
        }

        VirtualFile parent = selectedEditor.getFile().getParent();
        String parentPath = parent.getPath();

        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(new File(parentPath, name), true);
        if (virtualFile != null) {
            return null;
        }

        return WriteAction.computeAndWait(() -> {
            String content;
            if (isPrivate) {
                content = "{\n" +
                    "  \"dev\": {\n" +
                    "    \"token\": \"rRTJHGerfgET\"\n" +
                    "  },\n" +
                    "  \"uat\": {\n" +
                    "    \"token\": \"ERTYHGSDKFue\"\n" +
                    "  },\n" +
                    "  \"pro\": {\n" +
                    "    \"token\": \"efJFGHJKHYTR\"\n" +
                    "  }\n" +
                    "}";
            }
            else {
                content = "{\n" +
                    "  \"dev\": {\n" +
                    "    \"baseUrl\": \"http://localhost:8800\"\n" +
                    "  },\n" +
                    "  \"uat\": {\n" +
                    "    \"baseUrl\": \"https://uat.javamaster.org/bm-wash\"\n" +
                    "  },\n" +
                    "  \"pro\": {\n" +
                    "    \"baseUrl\": \"https://pro.javamaster.org/bm-wash\"\n" +
                    "  },\n" +
                    "  \"common\": {\n" +
                    "    \"contextPath\": \"/admin\"\n" +
                    "  }\n" +
                    "}";
            }

            try {
                PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(parent);
                if (psiDirectory == null) {
                    return null;
                }

                VirtualFile newJsonFile = psiDirectory.createFile(name).getVirtualFile();
                newJsonFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                return newJsonFile;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static EnvFileService getService(Project project) {
        return project.getService(EnvFileService.class);
    }

    private static String resolveValue(String value, String httpFileParentPath) {
        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String matchStr = matcher.group();

            var myJsonValue = TextVariableLazyFileElement.parse(matchStr);

            if (myJsonValue.getVariableList().isEmpty()) {
                matcher.appendReplacement(sb, escapeRegexp(matchStr));
                continue;
            }

            var variable = myJsonValue.getVariableList().get(0);
            var variableName = variable.getVariableName();
            if (variableName == null) {
                matcher.appendReplacement(sb, escapeRegexp(matchStr));
                continue;
            }

            var variableArgs = variable.getVariableArgs();
            Object[] args = variableArgs != null ? variableArgs.toArgsList() : ArrayUtil.EMPTY_OBJECT_ARRAY;
            String name = variableName.getName();

            // Support environment files that reference built-in variables
            InnerVariableEnum innerVariableEnum = InnerVariableEnum.getEnum(name);

            String result = (innerVariableEnum != null)
                ? innerVariableEnum.exec(httpFileParentPath, args)
                : matchStr;

            matcher.appendReplacement(sb, escapeRegexp(result));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Map<String, String> getEnvMapFromIndex(Project project, String selectedEnv, String httpFileParentPath, Module module) {
        if (selectedEnv == null) {
            return null;
        }

        GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);
        Map<String, String> map = getEnvMapFromIndex(selectedEnv, httpFileParentPath, projectScope);

        if (module != null) {
            GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
            map.putAll(getEnvMapFromIndex(selectedEnv, httpFileParentPath, moduleScope));
        }

        if (map.isEmpty()) {
            return null;
        }

        return map;
    }

    private static Map<String, String> getEnvMapFromIndex(String selectedEnv, String httpFileParentPath, GlobalSearchScope scope) {
        Map<String, String> map = new HashMap<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        List<Map<String, String>> commonList = fileBasedIndex.getValues(INDEX_ID, COMMON_ENV_NAME, scope);
        for (Map<String, String> it : commonList) {
            for (Map.Entry<String, String> entry : it.entrySet()) {
                map.put(entry.getKey(), resolveValue(entry.getValue(), httpFileParentPath));
            }
        }

        List<Map<String, String>> envList = fileBasedIndex.getValues(INDEX_ID, selectedEnv, scope);
        for (Map<String, String> it : envList) {
            for (Map.Entry<String, String> entry : it.entrySet()) {
                map.put(entry.getKey(), resolveValue(entry.getValue(), httpFileParentPath));
            }
        }

        return map;
    }

    public static Map<String, String> getEnvMap(Project project, boolean tryIndex) {
        var triple = HttpEditorTopForm.getTriple(project);
        if (triple == null) {
            return new HashMap<>();
        }

        String selectedEnv = triple.getFirst();
        String httpFileParentPath = triple.getSecond().getParent().getPath();
        Module module = triple.getThird();

        if (tryIndex) {
            Map<String, String> mapFromIndex = getEnvMapFromIndex(project, selectedEnv, httpFileParentPath, module);
            if (mapFromIndex != null) {
                return mapFromIndex;
            }
        }

        Map<String, String> map = new LinkedHashMap<>();

        map.putAll(getEnvMap(COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project));
        map.putAll(getEnvMap(COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project));
        map.putAll(getEnvMap(selectedEnv, httpFileParentPath, ENV_FILE_NAME, project));
        map.putAll(getEnvMap(selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project));

        return map;
    }

    public static Map<String, String> getEnvMap(Project project) {
        return getEnvMap(project, true);
    }

    private static Map<String, String> getEnvMap(String selectedEnv, String httpFileParentPath, String envFileName, Project project) {
        String env = selectedEnv != null ? selectedEnv : COMMON_ENV_NAME;

        JsonFile psiFile = getEnvJsonFile(envFileName, httpFileParentPath, project);
        if (psiFile == null) {
            return Collections.emptyMap();
        }

        JsonValue topLevelValue = psiFile.getTopLevelValue();
        if (!(topLevelValue instanceof JsonObject)) {
            System.err.println("The environment file: " + psiFile.getVirtualFile().getPath() + " outer format does not conform to the specification!");
            return Collections.emptyMap();
        }

        JsonProperty envProperty = ((JsonObject) topLevelValue).findProperty(env);
        if (envProperty == null) {
            return Collections.emptyMap();
        }

        JsonValue jsonValue = envProperty.getValue();
        if (!(jsonValue instanceof JsonObject)) {
            System.err.println("The environment file: " + psiFile.getVirtualFile().getPath() + " inner format does not conform to the specification!");
            return Collections.emptyMap();
        }

        EnvFileService envFileService = getService(project);
        Map<String, String> map = new LinkedHashMap<>();

        for (JsonProperty it : ((JsonObject) jsonValue).getPropertyList()) {
            String envValue = envFileService.getEnvValue(it.getName(), selectedEnv, httpFileParentPath);
            map.put(it.getName(), envValue != null ? envValue : "<null>");
        }

        return map;
    }

    public static JsonLiteral getEnvEleLiteral(String key, String selectedEnv, String httpFileParentPath, Project project) {
        JsonLiteral literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project);
        if (literal != null) {
            return literal;
        }

        literal = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, ENV_FILE_NAME, project);
        if (literal != null) {
            return literal;
        }

        literal = getEnvEleLiteral(key, COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project);
        if (literal != null) {
            return literal;
        }

        return getEnvEleLiteral(key, COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project);
    }

    private static JsonLiteral getEnvEleLiteral(String key, String selectedEnv, String httpFileParentPath, String envFileName, Project project) {
        JsonFile jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project);
        if (jsonFile == null) {
            return null;
        }

        JsonProperty envProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, envFileName, project);
        if (envProperty == null) {
            return null;
        }

        JsonValue jsonValue = envProperty.getValue();
        if (!(jsonValue instanceof JsonObject)) {
            System.err.println("The environment file: " + jsonFile.getVirtualFile().getPath() + " inner format does not conform to the specification!");
            return null;
        }

        JsonProperty jsonProperty = ((JsonObject) jsonValue).findProperty(key);
        if (jsonProperty == null) {
            return null;
        }

        JsonValue innerJsonValue = jsonProperty.getValue();
        if (innerJsonValue == null) {
            return null;
        }

        if (innerJsonValue instanceof JsonStringLiteral) {
            return (JsonStringLiteral) innerJsonValue;
        }
        else if (innerJsonValue instanceof JsonNumberLiteral) {
            return (JsonNumberLiteral) innerJsonValue;
        }
        else if (innerJsonValue instanceof JsonBooleanLiteral) {
            return (JsonBooleanLiteral) innerJsonValue;
        }
        else {
            System.err.println("The environment file: " + jsonFile.getVirtualFile().getPath() + " innermost format does not conform to the specification!!");
            return null;
        }
    }

    public static JsonProperty getEnvJsonProperty(String selectedEnv, String httpFileParentPath, Project project) {
        JsonProperty jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, ENV_FILE_NAME, project);
        if (jsonProperty != null) {
            return jsonProperty;
        }

        jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project);
        if (jsonProperty != null) {
            return jsonProperty;
        }

        jsonProperty = getEnvJsonProperty(COMMON_ENV_NAME, httpFileParentPath, ENV_FILE_NAME, project);
        if (jsonProperty != null) {
            return jsonProperty;
        }

        return getEnvJsonProperty(COMMON_ENV_NAME, httpFileParentPath, PRIVATE_ENV_FILE_NAME, project);
    }

    private static JsonProperty getEnvJsonProperty(String selectedEnv, String httpFileParentPath, String envFileName, Project project) {
        String env = selectedEnv != null ? selectedEnv : COMMON_ENV_NAME;

        JsonFile jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project);
        if (jsonFile == null) {
            return null;
        }

        JsonValue topLevelValue = jsonFile.getTopLevelValue();
        if (!(topLevelValue instanceof JsonObject)) {
            System.err.println("The environment file: " + jsonFile.getVirtualFile().getPath() + " outer format does not conform to the specification!");
            return null;
        }

        return ((JsonObject) topLevelValue).findProperty(env);
    }

    public static String getJsonLiteralValue(JsonLiteral literal) {
        if (literal instanceof JsonStringLiteral) {
            String txt = literal.getText();
            return txt.substring(1, txt.length() - 1);
        }
        else if (literal instanceof JsonNumberLiteral) {
            return String.valueOf(((JsonNumberLiteral) literal).getValue());
        }
        else if (literal instanceof JsonBooleanLiteral) {
            return String.valueOf(((JsonBooleanLiteral) literal).getValue());
        }
        else if (literal instanceof JsonNullLiteral) {
            return literal.getText();
        }
        else {
            System.err.println("error:" + literal);
            return "";
        }
    }

    public static JsonFile getEnvJsonFile(String envFileName, String httpFileParentPath, Project project) {
        String fileName = httpFileParentPath + "/" + envFileName;
        VirtualFile virtualFile = VfsUtil.findFileByIoFile(new File(fileName), true);

        if (virtualFile != null) {
            return (JsonFile) PsiUtil.getPsiFile(project, virtualFile);
        }

        fileName = project.getBasePath() + "/" + envFileName;
        virtualFile = VfsUtil.findFileByIoFile(new File(fileName), true);
        if (virtualFile != null) {
            return (JsonFile) PsiUtil.getPsiFile(project, virtualFile);
        }

        return null;
    }
}
