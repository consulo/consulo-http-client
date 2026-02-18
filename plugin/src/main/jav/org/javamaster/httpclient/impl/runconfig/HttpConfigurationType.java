package org.javamaster.httpclient.impl.runconfig;

import consulo.application.util.NotNullLazyValue;
import consulo.execution.configuration.ConfigurationTypeBase;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.impl.utils.HttpUtils;

/**
 * @author yudong
 */
public class HttpConfigurationType extends ConfigurationTypeBase {
    public HttpConfigurationType() {
        super(
            HttpUtils.HTTP_TYPE_ID,
            "HttpClient",
            "Use to send request",
            NotNullLazyValue.createConstantValue(HttpIcons.FILE)
        );
        addFactory(new HttpConfigurationFactory());
    }
}
