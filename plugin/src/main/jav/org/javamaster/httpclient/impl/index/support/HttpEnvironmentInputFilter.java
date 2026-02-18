package org.javamaster.httpclient.impl.index.support;

import consulo.json.JsonFileType;
import consulo.virtualFileSystem.VirtualFile;
import consulo.index.io.DefaultFileTypeSpecificInputFilter;
import org.javamaster.httpclient.impl.env.EnvFileService;

public class HttpEnvironmentInputFilter extends DefaultFileTypeSpecificInputFilter {
    public HttpEnvironmentInputFilter() {
        super(JsonFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(VirtualFile file) {
        return EnvFileService.ENV_FILE_NAMES.contains(file.getName());
    }
}
