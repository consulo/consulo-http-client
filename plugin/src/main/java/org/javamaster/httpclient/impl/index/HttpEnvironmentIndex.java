package org.javamaster.httpclient.impl.index;

import consulo.json.psi.*;
import consulo.index.io.*;
import consulo.index.io.data.DataExternalizer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.KeyDescriptor;
import org.javamaster.httpclient.impl.index.support.HttpEnvironmentInputFilter;
import org.javamaster.httpclient.impl.index.support.HttpVariablesExternalizer;
import org.javamaster.httpclient.impl.index.support.StringDataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpEnvironmentIndex extends FileBasedIndexExtension<String, Map<String, String>> {
    public static final ID<String, Map<String, String>> INDEX_ID = ID.create("http.execution.environment");

    @Override
    public @NotNull ID<String, Map<String, String>> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, Map<String, String>, FileContent> getIndexer() {
        return inputData -> {
            JsonFile file = (JsonFile) inputData.getPsiFile();

            JsonValue topLevelValue = file.getTopLevelValue();
            if (!(topLevelValue instanceof JsonObject)) {
                return new HashMap<>();
            }

            JsonObject root = (JsonObject) topLevelValue;
            Map<String, Map<String, String>> result = new HashMap<>();

            for (JsonProperty property : root.getPropertyList()) {
                JsonValue envValueObj = property.getValue();

                if (!(envValueObj instanceof JsonObject)) {
                    continue;
                }

                String env = property.getName();
                result.put(env, readEnvVariables((JsonObject) envValueObj));
            }

            return result;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<Map<String, String>> getValueExternalizer() {
        return new HttpVariablesExternalizer(new StringDataExternalizer(), HashMap::new);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new HttpEnvironmentInputFilter();
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private Map<String, String> readEnvVariables(@NotNull JsonObject envObj) {
        List<JsonProperty> properties = envObj.getPropertyList();
        if (properties.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> map = new HashMap<>();

        for (JsonProperty property : properties) {
            String key = property.getName();
            if (key.isBlank()) {
                continue;
            }

            JsonValue innerJsonValue = property.getValue();
            if (innerJsonValue instanceof JsonStringLiteral) {
                String text = innerJsonValue.getText();
                map.put(key, text.substring(1, text.length() - 1));
            } else if (innerJsonValue instanceof JsonNumberLiteral) {
                map.put(key, String.valueOf(((JsonNumberLiteral) innerJsonValue).getValue()));
            } else if (innerJsonValue instanceof JsonBooleanLiteral) {
                map.put(key, String.valueOf(((JsonBooleanLiteral) innerJsonValue).getValue()));
            } else {
                map.put(key, innerJsonValue != null ? innerJsonValue.getText() : "");
            }
        }

        return map;
    }
}
