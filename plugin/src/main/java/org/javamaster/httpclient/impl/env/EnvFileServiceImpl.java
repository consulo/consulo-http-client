package org.javamaster.httpclient.impl.env;

import consulo.annotation.component.ServiceImpl;
import consulo.httpClient.execution.env.HttpEnvironment;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.javamaster.httpclient.env.EnvFileService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Reads {@code http-client.env.json} and {@code http-client.private.env.json} next to the .http file.
 *
 * @author VISTALL
 * @since 2026-02-19
 */
@Singleton
@ServiceImpl
public class EnvFileServiceImpl implements EnvFileService {
    @Override
    public Set<String> getPresetEnvSet(String httpFileParentPath) {
        if (httpFileParentPath == null) {
            return Set.of();
        }

        Path directory = Path.of(httpFileParentPath);
        try {
            return HttpEnvironment.listEnvironments(HttpEnvironment.findEnvFile(directory, false), HttpEnvironment.findEnvFile(directory, true));
        }
        catch (IOException e) {
            return Set.of();
        }
    }

    @Nullable
    @Override
    public String getEnvValue(String key, String selectedEnv, String httpFileParentPath) {
        try {
            return HttpEnvironment.load(Path.of(httpFileParentPath), selectedEnv).get(key);
        }
        catch (IOException e) {
            return null;
        }
    }
}
