package consulo.httpClient.impl.internal.endpoint;

import consulo.endpoint.client.generator.ClientGenerator;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class HttpRequestClientGeneratorUtil {
    private HttpRequestClientGeneratorUtil() {
    }

    public static @Nullable ClientGenerator find(Project project) {
        return project.getExtensionPoint(ClientGenerator.class).findExtension(HttpRequestClientGenerator.class);
    }
}
