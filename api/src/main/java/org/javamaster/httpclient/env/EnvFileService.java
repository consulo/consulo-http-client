package org.javamaster.httpclient.env;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.project.Project;

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

    Set<String> getPresetEnvSet(String httpFileParentPath);
}
