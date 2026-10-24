/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javamaster.httpclient.impl.runconfig;

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-24
 */
@CompiledJson
public class HttpRunConfigurationOptions extends RunConfigurationOptions {
    private @Nullable String myHttpFilePath;
    private @Nullable String myEnv;
    private @Nullable String myRequestName;
    private boolean myRunAll;

    @Attribute("httpFilePath")
    public @Nullable String getHttpFilePath() {
        return myHttpFilePath;
    }

    public void setHttpFilePath(@Nullable String httpFilePath) {
        myHttpFilePath = StringUtil.nullize(httpFilePath, true);
    }

    @Attribute("env")
    public @Nullable String getEnv() {
        return myEnv;
    }

    public void setEnv(@Nullable String env) {
        myEnv = StringUtil.nullize(env, true);
    }

    @Attribute("requestName")
    public @Nullable String getRequestName() {
        return myRequestName;
    }

    public void setRequestName(@Nullable String requestName) {
        myRequestName = StringUtil.nullize(requestName, true);
    }

    @Attribute("runAll")
    public boolean isRunAll() {
        return myRunAll;
    }

    public void setRunAll(boolean runAll) {
        myRunAll = runAll;
    }
}
