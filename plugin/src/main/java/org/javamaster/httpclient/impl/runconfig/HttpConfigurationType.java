package org.javamaster.httpclient.impl.runconfig;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.utils.HttpUtilsPart;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpConfigurationType extends ConfigurationTypeBase {
    public HttpConfigurationType() {
        super(
            HttpUtilsPart.HTTP_TYPE_ID,
            LocalizeValue.localizeTODO("HTTP Client"),
            LocalizeValue.localizeTODO("Use to send request"),
            HttpIcons.FILE
        );
        addFactory(new ConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new HttpRunConfiguration(project, this, "");
            }
        });
    }
}
