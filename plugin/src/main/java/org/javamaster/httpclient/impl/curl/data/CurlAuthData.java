package org.javamaster.httpclient.impl.curl.data;

import consulo.util.lang.StringUtil;
import com.sun.security.auth.UserPrincipal;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;

import java.security.Principal;

public class CurlAuthData {
    private final AuthScope scope;
    private final Credentials authCredentials;

    public CurlAuthData(AuthScope scope, Credentials authCredentials) {
        this.scope = scope;
        this.authCredentials = authCredentials;
    }

    public Credentials getAuthCredentials() {
        return authCredentials;
    }

    @SuppressWarnings("unused")
    public boolean isSchemeEquals(String scheme) {
        return StringUtil.equalsIgnoreCase(scope.getScheme(), scheme);
    }

    public static final CurlAuthData EMPTY_CREDENTIALS = new CurlAuthData(AuthScope.ANY, new Credentials() {
        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public Principal getUserPrincipal() {
            return new UserPrincipal("");
        }
    });
}
