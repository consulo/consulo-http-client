package org.javamaster.httpclient.impl.key;

import consulo.util.dataholder.Key;

/**
 * @author yudong
 */
public class HttpKey {
    public static final Key<Boolean> httpDashboardBinaryBodyKey = 
        Key.create("org.javamaster.dashboard.httpDashboardBinaryBody");
    
    private HttpKey() {
    }
}
