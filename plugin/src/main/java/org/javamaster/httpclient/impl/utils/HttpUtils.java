package org.javamaster.httpclient.impl.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import consulo.application.ReadAction;
import consulo.application.util.function.Computable;
import consulo.document.util.TextRange;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.ConfigurationFromContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.fileEditor.FileEditorManager;
import consulo.httpClient.execution.HttpRequestSupport;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.Trinity;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.factory.HttpPsiFactory;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfiguration;
import org.javamaster.httpclient.impl.runconfig.HttpRunConfigurationProducer;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.model.HttpResponse;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.model.SimpleTypeEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.*;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HttpUtils extends HttpUtilsPart {

    public static final String WEB_BOUNDARY = "boundary";

    private static final String VARIABLE_SIGN_END = "}}";

    public static final Key<Runnable> gutterIconLoadingKey = Key.create("GUTTER_ICON_LOADING_KEY");
    public static final Key<Integer> requestFinishedKey = Key.create("REQUEST_FINISHED_KEY");

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    /**
     * Configuration of the request - the stored one, or a new one which is not stored, same as the run from the context creates
     */
    public static @Nullable RunnerAndConfigurationSettings findOrCreateConfiguration(HttpMethod httpMethod) {
        HttpRunConfigurationProducer producer = RunConfigurationProducer.getInstance(HttpRunConfigurationProducer.class);

        ConfigurationFromContext fromContext = ReadAction.compute(
            () -> producer.findOrCreateConfigurationFromContext(new ConfigurationContext(httpMethod), true)
        );
        return fromContext != null ? fromContext.getConfigurationSettings() : null;
    }

    /**
     * Configuration of the request, stored and selected - the run from the gutter keeps it
     */
    public static @Nullable RunnerAndConfigurationSettings saveConfiguration(
        Project project,
        @Nullable String selectedEnv,
        HttpMethod httpMethod
    ) {
        RunnerAndConfigurationSettings configurationSettings = findOrCreateConfiguration(httpMethod);
        if (configurationSettings == null) {
            return null;
        }

        //configurationSettings.setActivateToolWindowBeforeRun(false);

        // the run from the gutter is with the environment selected in the editor, not with the one of the previous run
        HttpRunConfiguration httpRunConfiguration = (HttpRunConfiguration) configurationSettings.getConfiguration();
        httpRunConfiguration.setEnv(StringUtil.notNullize(selectedEnv));

        RunManager runManager = RunManager.getInstance(project);
        if (runManager.findSettings(httpRunConfiguration) == null) {
            runManager.addConfiguration(configurationSettings);
        }

        runManager.setSelectedConfiguration(configurationSettings);

        return configurationSettings;
    }

//    public static HttpMessageBody getInjectHost(JsonStringLiteral jsonString, Project project) {
//        if (!jsonString.isPropertyName()) {
//            return null;
//        }
//
//        PsiElement injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(jsonString);
//        if (!(injectionHost instanceof HttpMessageBody)) {
//            return null;
//        }
//
//        return (HttpMessageBody) injectionHost;
//    }

    public static String handleOrdinaryContentCurl(
        HttpRequestMessagesGroup requestMessagesGroup,
        VariableResolver variableResolver,
        HttpHeader header,
        boolean raw
    ) {
        String reqStr = "";

        HttpMessageBody messageBody = requestMessagesGroup.getMessageBody();
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.getText());
        }

        HttpInputFile inputFile = requestMessagesGroup.getInputFile();
        String filePath = inputFile != null && inputFile.getFilePath() != null
            ? inputFile.getFilePath().getText()
            : null;

        if (filePath == null) {
            return raw ? reqStr + CR_LF : reqStr.replace("\n", "\n    ").replace("'", "'\\''");
        }

        String path = constructFilePath(filePath, variableResolver.getHttpFileParentPath());

        File file = new File(path);

        if (!HttpRequestSupport.isTxtContentType(header)) {
            return "";
        }

        reqStr += CR_LF;

        String str = null;
        try {
            str = VirtualFileUtils.readNewestContent(file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        reqStr += variableResolver.resolve(str);

        return raw ? reqStr + CR_LF : reqStr.replace("\n", "\n    ").replace("'", "'\\''");
    }

    public static List<String> constructMultipartBodyCurl(
        HttpMultipartMessage httpMultipartMessage,
        VariableResolver variableResolver,
        String boundary,
        boolean raw
    ) {
        List<String> list = new ArrayList<>();

        httpMultipartMessage.getMultipartFieldList().forEach(it -> {
            HttpRequestMessagesGroup requestMessagesGroup = it.getRequestMessagesGroup();
            HttpHeader header = it.getHeader();

            if (raw) {
                list.add("--" + boundary + CR_LF);

                header.getHeaderFieldList().forEach(innerIt -> {
                    list.add(innerIt.getName() + ": " + innerIt.getValue() + CR_LF);
                });

                list.add(CR_LF);
            }

            HttpMessageBody messageBody = requestMessagesGroup.getMessageBody();
            if (messageBody != null) {
                String content = variableResolver.resolve(messageBody.getText());

                if (raw) {
                    list.add(content + CR_LF);
                }
                else {
                    String contentTypeText = header.getContentTypeField() != null
                        && header.getContentTypeField().getHeaderFieldValue() != null
                        ? header.getContentTypeField().getHeaderFieldValue().getText()
                        : "";
                    list.add("    -F \"" + header.getContentDispositionName() + "=" + content + ";type=" + contentTypeText + "\"");
                }
            }

            HttpInputFile inputFile = requestMessagesGroup.getInputFile();
            String filePath = inputFile != null && inputFile.getFilePath() != null
                ? inputFile.getFilePath().getText()
                : null;

            if (filePath != null) {
                String path = constructFilePath(filePath, variableResolver.getHttpFileParentPath());

                File file = new File(path);

                String content = "@" + file.getAbsolutePath().replace("\\", "/");

                if (raw) {
                    list.add("< " + file.getAbsolutePath() + CR_LF);
                }
                else {
                    String contentTypeText = header.getContentTypeField() != null
                        && header.getContentTypeField().getHeaderFieldValue() != null
                        ? header.getContentTypeField().getHeaderFieldValue().getText()
                        : "";
                    list.add("    -F \"" + header.getContentDispositionName() + "=" + content + ";filename="
                        + header.getContentDispositionFileName() + ";type=" + contentTypeText + "\"");
                }
            }
        });

        if (raw) {
            list.add("--" + boundary + "--");
        }

        return list;
    }

    public static PsiElement resolveFileGlobalVariable(String variableName, PsiFile httpFile) {
        Collection<HttpGlobalVariable> globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable.class);

        return globalVariables.stream()
            .map(it -> {
                HttpGlobalVariableName globalVariableName = it.getGlobalVariableName();
                if (globalVariableName.getName().equals(variableName)) {
                    return globalVariableName;
                }
                else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    public static List<HttpScriptBody> getAllPostJsScripts(PsiFile httpFile) {
        Collection<HttpResponseHandler> handlers = PsiTreeUtil.findChildrenOfType(httpFile, HttpResponseHandler.class);

        return handlers.stream()
            .map(HttpRequestSupport::getJsScript)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Pair<String, TextRange> getSearchTxtInfo(HttpRequestTarget requestTarget, String httpFileParentPath) {
        Project project = requestTarget.getProject();

        String url = requestTarget.getText();

        int start;
        int bracketIdx = url.indexOf(VARIABLE_SIGN_END);
        if (bracketIdx != -1) {
            start = bracketIdx + 2;
        }
        else {
            EnvFileService envFileService = EnvFileService.getService(project);
            String selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

            String contextPath = envFileService.getEnvValue("contextPath", selectedEnv, httpFileParentPath);
            String contextPathTrim = envFileService.getEnvValue("contextPathTrim", selectedEnv, httpFileParentPath);

            int tmpIdx;
            URI uri;
            try {
                uri = new URI(url);
                if (contextPath != null) {
                    tmpIdx = url.indexOf(contextPath);
                }
                else if (contextPathTrim != null) {
                    tmpIdx = url.indexOf(contextPathTrim) + contextPathTrim.length();
                }
                else {
                    tmpIdx = url.indexOf(uri.getPath());
                }
            }
            catch (Exception e) {
                return null;
            }
            start = tmpIdx;
        }

        if (start == -1) {
            return null;
        }

        int idx = url.lastIndexOf("?");
        int end = idx == -1 ? url.length() : idx;

        if (end < start) {
            return null;
        }

        TextRange textRange = new TextRange(start, end);
        String searchTxt = url.substring(start, end);
        return Pair.create(searchTxt, textRange);
    }

    public static boolean isHistoryFile(VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.getNameWithoutExtension().endsWith("history");
    }

//    public static LinkedList<String> collectJsonPropertyNameLevels(JsonStringLiteral jsonString) {
//        LinkedList<String> beanFieldLevels = new LinkedList<>();
//
//        JsonProperty jsonProperty = PsiTreeUtil.getParentOfType(jsonString, JsonProperty.class);
//        while (jsonProperty != null) {
//            String propertyName = getJsonPropertyName(jsonProperty);
//            beanFieldLevels.push(propertyName);
//            jsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty.class);
//        }
//
//        return beanFieldLevels;
//    }

//    private static String getJsonPropertyName(JsonProperty jsonProperty) {
//        JsonElement nameElement = jsonProperty.getNameElement();
//        String name = nameElement.getText();
//        return name.substring(1, name.length() - 1);
//    }
//
//    public static PsiParameter resolveTargetParam(PsiMethod psiMethod) {
//        PsiMethod[] superPsiMethods = psiMethod.findSuperMethods(false);
//        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
//        PsiParameter psiParameter = null;
//
//        for (int index = 0; index < psiParameters.length; index++) {
//            PsiParameter psiParam = psiParameters[index];
//            boolean hasAnno = psiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME);
//            if (hasAnno) {
//                psiParameter = psiParam;
//                break;
//            }
//
//            for (PsiMethod superPsiMethod : superPsiMethods) {
//                PsiParameter superPsiParam = superPsiMethod.getParameterList().getParameters()[index];
//                hasAnno = superPsiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME);
//                if (hasAnno) {
//                    psiParameter = psiParam;
//                    break;
//                }
//            }
//        }
//
//        return psiParameter;
//    }
//
//    public static PsiField resolveTargetField(
//        PsiClass paramPsiCls,
//        LinkedList<String> jsonPropertyNameLevels,
//        PsiType[] classGenericParameters
//    ) {
//        PsiField psiField = null;
//
//        try {
//            PsiClass fieldTypeCls;
//            String propertyName = jsonPropertyNameLevels.pop();
//
//            boolean isCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection");
//            if (isCollection) {
//                if (classGenericParameters.length == 0) {
//                    return null;
//                }
//
//                // Get the generic parameter type
//                fieldTypeCls = PsiUtils.resolvePsiType(classGenericParameters[0]);
//                if (fieldTypeCls == null) {
//                    return null;
//                }
//            } else {
//                fieldTypeCls = paramPsiCls;
//            }
//
//            while (true) {
//                psiField = fieldTypeCls.findFieldByName(propertyName, true);
//                if (psiField == null) {
//                    return null;
//                }
//
//                if (!(psiField.getType() instanceof PsiClassType)) {
//                    return psiField;
//                }
//
//                PsiClassType psiType = (PsiClassType) psiField.getType();
//
//                PsiType[] parameters = psiType.getParameters();
//                if (parameters.length > 0) {
//                    // Get the generic parameter type
//                    fieldTypeCls = PsiUtils.resolvePsiType(parameters[0]);
//                    if (fieldTypeCls == null) {
//                        return null;
//                    }
//                } else {
//                    PsiClass psiFieldTypeCls = PsiUtils.resolvePsiType(psiType);
//                    if (psiFieldTypeCls == null) {
//                        return null;
//                    }
//
//                    if (psiFieldTypeCls instanceof PsiTypeParameter && classGenericParameters.length > 0) {
//                        // The parameter itself is a generic type, such as T, and the first one is taken directly
//                        PsiClassType genericActualType = (PsiClassType) classGenericParameters[0];
//                        if (genericActualType.getParameters().length > 0) {
//                            PsiClass psiFieldGenericTypeCls = PsiUtils.resolvePsiType(genericActualType.getParameters()[0]);
//                            if (psiFieldGenericTypeCls == null) {
//                                return null;
//                            }
//                            fieldTypeCls = psiFieldGenericTypeCls;
//                        } else {
//                            fieldTypeCls = PsiUtils.resolvePsiType(genericActualType);
//                            if (fieldTypeCls == null) {
//                                return null;
//                            }
//                        }
//                    } else {
//                        fieldTypeCls = psiFieldTypeCls;
//                    }
//                }
//
//                propertyName = jsonPropertyNameLevels.pop();
//            }
//        } catch (NoSuchElementException e) {
//            // End of iteration
//        }
//
//        return psiField;
//    }

    public static String convertToJsString(String str) {
        return "`" + str.replace("\\", "\\\\").replace("`", "\\`") + "`";
    }

    public static Object convertReqBody(Object reqBody) {
        if (reqBody == null) {
            return "null";
        }

        if (reqBody instanceof String) {
            return convertToJsString((String) reqBody);
        }

        if (reqBody instanceof Pair) {
            @SuppressWarnings("unchecked")
            Pair<byte[], String> pair = (Pair<byte[], String>) reqBody;

            return pair.first;
        }

        if (reqBody instanceof List) {
            @SuppressWarnings("unchecked")
            List<Pair<byte[], String>> list = (List<Pair<byte[], String>>) reqBody;

            return list.stream()
                .map(it -> it.first)
                .reduce((a, b) -> {
                    byte[] combined = new byte[a.length + b.length];
                    System.arraycopy(a, 0, combined, 0, a.length);
                    System.arraycopy(b, 0, combined, a.length, b.length);
                    return combined;
                })
                .orElse(new byte[0]);
        }

        throw new IllegalArgumentException(NlsBundle.message("reqBody.unknown", reqBody.getClass().toString()));
    }


    public static PsiElement createGlobalVariableAndInsert(String variableName, String variableValue, Project project) {
        var textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (textEditor == null) {
            return null;
        }

        HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());

        PsiElement newGlobalVariable = HttpPsiFactory.createGlobalVariable(variableName, variableValue, project);

        List<HttpDirectionComment> directionComments = httpFile.getDirectionComments();
        HttpGlobalHandler globalHandler = httpFile.getGlobalHandler();

        PsiElement elementCopy;
        if (!directionComments.isEmpty()) {
            elementCopy = httpFile.addAfter(newGlobalVariable, directionComments.get(directionComments.size() - 1).getNextSibling());
        }
        else if (globalHandler != null) {
            elementCopy = httpFile.addAfter(newGlobalVariable, globalHandler);
        }
        else {
            elementCopy = httpFile.addBefore(newGlobalVariable, httpFile.getFirstChild());
        }

        PsiElement whitespace = newGlobalVariable.getNextSibling();
        elementCopy.add(whitespace);

        PsiElement cr = whitespace.getNextSibling();
        if (cr != null) {
            elementCopy.add(cr);
        }

        return elementCopy;
    }

    public static boolean modifyFileGlobalVariable(
        String key,
        String newKey,
        String newValue,
        boolean add,
        Project project
    ) {
        return WriteCommandAction.runWriteCommandAction(project, (Computable<Boolean>) () -> {
            if (add) {
                PsiElement variable = createGlobalVariableAndInsert(newKey, newValue, project);

                return variable != null;
            }
            else {
                var textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (textEditor == null) {
                    return false;
                }

                HttpFile httpFile = (HttpFile) PsiUtilCore.getPsiFile(project, textEditor.getVirtualFile());
                Collection<HttpGlobalVariable> children = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable.class);

                HttpGlobalVariable globalVariable = children.stream()
                    .filter(it -> it.getGlobalVariableName().getName().equals(key))
                    .findFirst()
                    .orElse(null);

                if (globalVariable == null) {
                    return false;
                }

                if (!key.equals(newKey)) {
                    RenameProcessor renameProcessor = new RenameProcessor(
                        project, globalVariable, newKey, false, true
                    );
                    renameProcessor.run();
                }

                PsiElement newGlobalVariable = HttpPsiFactory.createGlobalVariable(newKey, newValue, project);

                globalVariable.replace(newGlobalVariable);
            }

            return true;
        });
    }
//
//    public static void modifyJsVariable(String newKey, String newValue) {
//        JsExecutor.setGlobalVariable(newKey, newValue);
//    }
//
//    public static boolean modifyEnvVariable(
//        String key,
//        String newKey,
//        String newValue,
//        boolean add,
//        Project project
//    ) {
//        var triple = HttpEditorTopForm.getTriple(project);
//        if (triple == null) {
//            return false;
//        }
//
//        String selectedEnv = triple.first;
//        String httpFileParentPath = triple.second.getParent().getPath();
//
//        if (add) {
//            JsonProperty jsonProperty = EnvFileService.getEnvJsonProperty(selectedEnv, httpFileParentPath, project);
//            if (jsonProperty == null) {
//                return false;
//            }
//
//            JsonValue jsonValue = jsonProperty.getValue();
//            if (!(jsonValue instanceof JsonObject)) {
//                return false;
//            }
//
//            JsonObject jsonObject = (JsonObject) jsonValue;
//
//            WriteCommandAction.runWriteCommandAction(project, () -> {
//                JsonProperty newProperty = JsonPsiFactory.createStringProperty(project, newKey, newValue);
//                PsiElement newComma = HttpPsiUtils.getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false);
//                List<JsonProperty> propertyList = jsonObject.getPropertyList();
//
//                if (propertyList.isEmpty()) {
//                    jsonObject.addAfter(newProperty, jsonObject.getFirstChild());
//                } else {
//                    PsiElement psiElement = jsonObject.addAfter(newComma, propertyList.get(propertyList.size() - 1));
//                    jsonObject.addAfter(newProperty, psiElement);
//                }
//            });
//        } else {
//            JsonElement jsonLiteral = EnvFileService.getEnvEleLiteral(key, selectedEnv, httpFileParentPath, project);
//            if (jsonLiteral == null) {
//                return false;
//            }
//
//            PsiElement jsonProperty = jsonLiteral.getParent();
//
//            if (!key.equals(newKey)) {
//                RenameProcessor renameProcessor = new RenameProcessor(
//                    project, jsonProperty, newKey,
//                    GlobalSearchScope.projectScope(project), false, true
//                );
//                renameProcessor.run();
//            }
//
//            WriteCommandAction.runWriteCommandAction(project, () -> {
//                JsonProperty newProperty;
//                if (jsonLiteral instanceof JsonNumberLiteral) {
//                    newProperty = JsonPsiFactory.createNumberProperty(project, newKey, newValue);
//                } else if (jsonLiteral instanceof JsonBooleanLiteral) {
//                    newProperty = JsonPsiFactory.createBoolProperty(project, newKey, newValue);
//                } else {
//                    newProperty = JsonPsiFactory.createStringProperty(project, newKey, newValue);
//                }
//
//                jsonProperty.replace(newProperty);
//            });
//        }
//
//        return true;
//    }
}
