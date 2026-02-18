package org.javamaster.httpclient.impl.runconfig;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.SimpleConfigurationType;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.utils.HttpUtilsPart;

/**
 * @author yudong
 */
public class HttpConfigurationFactory extends ConfigurationFactory {
    public HttpConfigurationFactory() {
        super(
            HttpUtilsPart.HTTP_TYPE_ID,
            LocalizeValue.localizeTODO("HttpClient"),
            LocalizeValue.of("Use to send request"),
            HttpIcons.FILE
        );
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new HttpRunConfiguration(project, this, "");
    }
}
