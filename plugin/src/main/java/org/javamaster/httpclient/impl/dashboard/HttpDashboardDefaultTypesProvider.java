package org.javamaster.httpclient.impl.dashboard;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.dashboard.RunDashboardDefaultTypesProvider;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import org.javamaster.httpclient.utils.HttpUtilsPart;

import java.util.Collection;

@ExtensionImpl
public class HttpDashboardDefaultTypesProvider implements RunDashboardDefaultTypesProvider {

    @Override
    public Collection<String> getDefaultTypeIds(Project project) {
        return new SmartList<>(HttpUtilsPart.HTTP_TYPE_ID);
    }
}
