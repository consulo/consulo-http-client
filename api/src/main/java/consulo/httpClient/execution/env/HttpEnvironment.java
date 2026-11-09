package consulo.httpClient.execution.env;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Variables of one environment, merged from {@code http-client.env.json}, {@code http-client.private.env.json}
 * and explicit overrides.
 * <p>
 * Priority, highest first: private overrides, public overrides, private file, public file. Inside a file the
 * selected environment wins over the shared sections {@code common} and {@code $shared}.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
public final class HttpEnvironment {
    public static final String ENV_FILE_NAME = "http-client.env.json";
    public static final String PRIVATE_ENV_FILE_NAME = "http-client.private.env.json";

    public static final List<String> SHARED_ENV_NAMES = List.of("$shared", "common");

    public static final HttpEnvironment EMPTY = new HttpEnvironment(null, Map.of());

    private final @Nullable String myName;
    private final Map<String, String> myVariables;

    public HttpEnvironment(@Nullable String name, Map<String, String> variables) {
        myName = name;
        myVariables = Collections.unmodifiableMap(new LinkedHashMap<>(variables));
    }

    public @Nullable String getName() {
        return myName;
    }

    public @Nullable String get(String key) {
        return myVariables.get(key);
    }

    public Map<String, String> asMap() {
        return myVariables;
    }

    /**
     * Loads the environment from the env files next to a .http file.
     */
    public static HttpEnvironment load(Path httpFileDirectory, @Nullable String name) throws IOException {
        return load(findEnvFile(httpFileDirectory, false), findEnvFile(httpFileDirectory, true), name, Map.of(), Map.of(), false);
    }

    /**
     * @param publicFile       public env file, null to skip
     * @param privateFile      private env file, null to skip
     * @param name             selected environment, null for shared variables only
     * @param publicOverrides  variables from command line, override file values
     * @param privateOverrides private variables from command line, override everything
     * @param strict           fail when {@code name} is not defined in any of the files
     */
    public static HttpEnvironment load(@Nullable Path publicFile,
                                       @Nullable Path privateFile,
                                       @Nullable String name,
                                       Map<String, String> publicOverrides,
                                       Map<String, String> privateOverrides,
                                       boolean strict) throws IOException {
        Map<String, Map<String, String>> publicEnvs = readEnvFile(publicFile);
        Map<String, Map<String, String>> privateEnvs = readEnvFile(privateFile);

        if (strict && name != null && !publicEnvs.containsKey(name) && !privateEnvs.containsKey(name)) {
            Set<String> known = new TreeSet<>();
            known.addAll(publicEnvs.keySet());
            known.addAll(privateEnvs.keySet());
            known.removeAll(SHARED_ENV_NAMES);
            throw new IllegalArgumentException("Environment '" + name + "' is not defined"
                + (known.isEmpty() ? "" : ". Known environments: " + String.join(", ", known)));
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (String sharedName : SHARED_ENV_NAMES) {
            result.putAll(publicEnvs.getOrDefault(sharedName, Map.of()));
        }
        for (String sharedName : SHARED_ENV_NAMES) {
            result.putAll(privateEnvs.getOrDefault(sharedName, Map.of()));
        }
        if (name != null) {
            result.putAll(publicEnvs.getOrDefault(name, Map.of()));
            result.putAll(privateEnvs.getOrDefault(name, Map.of()));
        }
        result.putAll(publicOverrides);
        result.putAll(privateOverrides);
        return new HttpEnvironment(name, result);
    }

    /**
     * Names of environments defined in the given files, without the shared sections.
     */
    public static Set<String> listEnvironments(@Nullable Path publicFile, @Nullable Path privateFile) throws IOException {
        Set<String> names = new TreeSet<>();
        names.addAll(readEnvFile(publicFile).keySet());
        names.addAll(readEnvFile(privateFile).keySet());
        names.removeAll(SHARED_ENV_NAMES);
        return names;
    }

    public static @Nullable Path findEnvFile(Path directory, boolean privateFile) {
        Path path = directory.resolve(privateFile ? PRIVATE_ENV_FILE_NAME : ENV_FILE_NAME);
        return Files.isRegularFile(path) ? path : null;
    }

    private static Map<String, Map<String, String>> readEnvFile(@Nullable Path file) throws IOException {
        if (file == null) {
            return Map.of();
        }

        JsonObject root;
        try {
            JsonElement element = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                throw new IOException(file + ": JSON object expected");
            }
            root = element.getAsJsonObject();
        }
        catch (JsonParseException e) {
            throw new IOException(file + ": " + e.getMessage(), e);
        }

        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            Map<String, String> variables = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> variable : entry.getValue().getAsJsonObject().entrySet()) {
                JsonElement value = variable.getValue();
                variables.put(variable.getKey(), value.isJsonPrimitive() ? value.getAsString() : value.toString());
            }
            result.put(entry.getKey(), variables);
        }
        return result;
    }
}
