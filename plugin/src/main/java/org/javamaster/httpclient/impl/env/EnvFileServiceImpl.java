package org.javamaster.httpclient.impl.env;

import consulo.annotation.component.ServiceImpl;
import jakarta.inject.Singleton;
import org.javamaster.httpclient.env.EnvFileService;

import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-02-19
 */
@Singleton
@ServiceImpl
public class EnvFileServiceImpl implements EnvFileService {
    // TODO !
    @Override
    public Set<String> getPresetEnvSet(String httpFileParentPath) {
        return Set.of();
    }
}
