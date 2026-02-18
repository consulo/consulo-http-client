package org.javamaster.httpclient.impl.dashboard;

import consulo.execution.dashboard.RunDashboardDefaultTypesProvider;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import org.javamaster.httpclient.impl.utils.HttpUtils;

import java.util.Collection;

public class HttpDashboardDefaultTypesProvider implements RunDashboardDefaultTypesProvider {

    @Override
    public Collection<String> getDefaultTypeIds(Project project) {
        return new SmartList<>(HttpUtils.HTTP_TYPE_ID);
    }
}
