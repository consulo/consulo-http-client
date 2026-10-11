package org.javamaster.httpclient.run;

import consulo.localize.LocalizeValue;

/**
 * @author VISTALL
 * @since 2026-01-19
 */
public interface HttpRunConfigurationApi {
    String getHttpFilePath();

    /**
     * Tab name of the request in {@link #getHttpFilePath()} - not the name of the configuration, which the run manager
     * may make unique
     */
    LocalizeValue getRequestName();
}
