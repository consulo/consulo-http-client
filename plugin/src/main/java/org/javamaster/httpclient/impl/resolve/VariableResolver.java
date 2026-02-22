package org.javamaster.httpclient.impl.resolve;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;
import org.javamaster.httpclient.impl.js.JsExecutor;
import org.javamaster.httpclient.impl.psi.TextVariableLazyFileElement;
import org.javamaster.httpclient.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolve variable
 *
 * @author yudong
 */
public class  VariableResolver {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("(\\{\\{[^{}]+}})");
    public static final String PROPERTY_PREFIX = "$property";
    public static final String ENV_PREFIX = "$env";

    private final JsExecutor jsExecutor;
    private final PsiFile httpFile;
    private final String selectedEnv;
    private final Project project;
    private final String httpFileParentPath;
    private final LinkedHashMap<String, String> fileScopeVariableMap;

    public VariableResolver(
            @Nullable JsExecutor jsExecutor,
            PsiFile httpFile,
            @Nullable String selectedEnv,
            Project project
    ) {
        this.jsExecutor = jsExecutor;
        this.httpFile = httpFile;
        this.selectedEnv = selectedEnv;
        this.project = project;
        this.httpFileParentPath = httpFile.getVirtualFile().getParent().getPath();
        this.fileScopeVariableMap = getFileGlobalVariables();
    }

    public String getHttpFileParentPath() {
        return httpFileParentPath;
    }

    public LinkedHashMap<String, String> getFileScopeVariableMap() {
        return fileScopeVariableMap;
    }

    public LinkedHashMap<String, String> getFileGlobalVariables() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        Collection<HttpGlobalVariable> globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable.class);

        for (HttpGlobalVariable globalVariable : globalVariables) {
            String name = globalVariable.getGlobalVariableName().getName();
            HttpGlobalVariableValue globalVariableValue = globalVariable.getGlobalVariableValue();
            if (globalVariableValue == null) {
                continue;
            }

            StringBuilder value = new StringBuilder();
            for (PsiElement innerIt : globalVariableValue.getChildren()) {
                if (innerIt instanceof HttpVariable) {
                    HttpVariable variable = (HttpVariable) innerIt;
                    HttpVariableName variableName = variable.getVariableName();

                    if (variableName == null) {
                        value.append(innerIt.getText());
                    } else {
                        HttpVariableArgs variableArgs = variable.getVariableArgs();
                        Object[] args = variableArgs != null ? variableArgs.toArgsList() : null;
                        String resolved = resolveVariable(
                                variableName.getName(),
                                new LinkedHashMap<>(),
                                variableName.isBuiltin(),
                                args
                        );
                        value.append(resolved != null ? resolved : innerIt.getText());
                    }
                } else if (innerIt instanceof HttpGlobalLiteralValue) {
                    value.append(innerIt.getText());
                }
            }

            map.put(name, value.toString());
        }

        return map;
    }

    public String resolve(String str) {
        Matcher matcher = VARIABLE_PATTERN.matcher(str);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String matchStr = matcher.group();

            HttpMyJsonValue myJsonValue = TextVariableLazyFileElement.parse(matchStr);

            List<HttpVariable> variableList = myJsonValue.getVariableList();
            if (variableList.isEmpty()) {
                matcher.appendReplacement(result, escapeRegexp(matchStr));
                continue;
            }

            HttpVariable variable = variableList.get(0);
            HttpVariableName variableName = variable.getVariableName();
            if (variableName == null) {
                matcher.appendReplacement(result, escapeRegexp(matchStr));
                continue;
            }

            String name = variableName.getName();
            boolean builtin = variableName.isBuiltin();
            HttpVariableArgs variableArgs = variable.getVariableArgs();
            Object[] args = variableArgs != null ? variableArgs.toArgsList() : null;

            String resolvedResult = resolveVariable(name, fileScopeVariableMap, builtin, args);
            matcher.appendReplacement(result, escapeRegexp(resolvedResult != null ? resolvedResult : matchStr));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Nullable
    private String resolveVariable(
            @Nullable String variable,
            Map<String, String> fileMap,
            boolean builtin,
            @Nullable Object[] args
    ) {
        if (variable == null) {
            return null;
        }

        if (builtin) {
            String innerVariable = resolveInnerVariable(variable, args);
            if (innerVariable != null) {
                return innerVariable;
            }

            if (variable.startsWith(PROPERTY_PREFIX)) {
                innerVariable = System.getProperty(variable.substring(PROPERTY_PREFIX.length() + 1));
                if (innerVariable != null) {
                    return innerVariable;
                }
            }

            if (variable.startsWith(ENV_PREFIX)) {
                innerVariable = System.getenv(variable.substring(ENV_PREFIX.length() + 1));
                if (innerVariable != null) {
                    return innerVariable;
                }
            }

            return null;
        }

        String innerVariable = fileMap.get(variable);
        if (innerVariable != null) {
            return innerVariable;
        }

        if (jsExecutor != null) {
            innerVariable = jsExecutor.getRequestVariable(variable);
            if (innerVariable != null) {
                return innerVariable;
            }

            innerVariable = jsExecutor.getJsGlobalVariable(variable);
            if (innerVariable != null) {
                return innerVariable;
            }
        }

        EnvFileService envFileService = EnvFileService.getService(project);
        String envValue = envFileService.getEnvValue(variable, selectedEnv, httpFileParentPath);
        if (envValue != null) {
            return envValue;
        }

        return null;
    }

    public LinkedHashMap<String, String> getJsGlobalVariables() {
        if (jsExecutor == null) {
            return new LinkedHashMap<>();
        }

        Map<String, String> globalVariables = jsExecutor.getJsGlobalVariables();
        if (globalVariables == null) {
            return new LinkedHashMap<>();
        }

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.putAll(globalVariables);

        return map;
    }

    @Nullable
    private String resolveInnerVariable(String variable, @Nullable Object[] args) {
        InnerVariableEnum variableEnum = InnerVariableEnum.getEnum(variable);
        if (variableEnum == null) {
            return null;
        }

        try {
            return variableEnum.exec(httpFileParentPath, args != null ? args : new Object[0]);
        } catch (UnsupportedOperationException e) {
            return variableEnum.exec(httpFileParentPath, project);
        }
    }

    public static String escapeRegexp(String result) {
        return result.replace("\\", "\\\\")
                .replace("$", "\\$");
    }

    public static String resolveInnerVariable(String str, String parentPath, Project project) {
        Matcher matcher = VARIABLE_PATTERN.matcher(str);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String matchStr = matcher.group();

            HttpMyJsonValue myJsonValue = TextVariableLazyFileElement.parse(matchStr);

            List<HttpVariable> variableList = myJsonValue.getVariableList();
            if (variableList.isEmpty()) {
                matcher.appendReplacement(result, escapeRegexp(matchStr));
                continue;
            }

            HttpVariable variable = variableList.get(0);
            HttpVariableName variableName = variable.getVariableName();
            if (variableName == null) {
                matcher.appendReplacement(result, escapeRegexp(matchStr));
                continue;
            }

            String name = variableName.getName();

            InnerVariableEnum variableEnum = InnerVariableEnum.getEnum(name);
            String resolvedResult = variableEnum != null ? variableEnum.exec(parentPath, project) : matchStr;

            matcher.appendReplacement(result, escapeRegexp(resolvedResult));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
