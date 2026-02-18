package org.javamaster.httpclient.impl.factory;

import consulo.json.lang.JsonLanguage;
import consulo.json.psi.JsonProperty;
import consulo.project.Project;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;

public class JsonPsiFactory {

    public static JsonProperty createStringProperty(Project project, String key, String value) {
        String text = "{\n" +
                "    \"" + key + "\": \"" + value + "\",\n" +
                "}";

        PsiFile tmpFile = createJsonFile(project, text);

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty.class);
    }

    public static JsonProperty createNumberProperty(Project project, String key, String value) {
        if (!StringUtils.isNumeric(value)) {
            throw new RuntimeException(key + " is a numeric property!");
        }

        String text = "{\n" +
                "    \"" + key + "\": " + value + ",\n" +
                "}";

        PsiFile tmpFile = createJsonFile(project, text);

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty.class);
    }

    public static JsonProperty createBoolProperty(Project project, String key, String value) {
        if (!"true".equals(value) && !"false".equals(value)) {
            throw new RuntimeException(key + " is boolean property!");
        }

        String text = "{\n" +
                "    \"" + key + "\": " + value + ",\n" +
                "}";

        PsiFile tmpFile = createJsonFile(project, text);

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty.class);
    }

    private static PsiFile createJsonFile(Project project, String text) {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        return psiFileFactory.createFileFromText("dummy.json", JsonLanguage.INSTANCE, text);
    }

    private JsonPsiFactory() {
    }
}
