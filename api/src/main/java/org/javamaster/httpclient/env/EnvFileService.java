package org.javamaster.httpclient.env;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
@ServiceAPI(ComponentScope.PROJECT)
public interface EnvFileService {
    static EnvFileService getService(Project project) {
        return project.getInstance(EnvFileService.class);
    }

    static Map<String, String> getEnvMapFromIndex(String selectedEnv, String httpFileParentPath, GlobalSearchScope scope) {
        return Map.of();
    }

    static Map<String, String> getEnvMap(Project project, boolean b) {
        return Map.of();
    }

    Set<String> getPresetEnvSet(String httpFileParentPath);

    @Nullable
    String getEnvValue(String key, String selectedEnv, String httpFileParentPath);
}
