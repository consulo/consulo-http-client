/**
 * @author VISTALL
 * @since 2025-07-21
 */
module consulo.rest.client.api {
    requires consulo.ide.api;

    requires transitive org.apache.httpcomponents.httpcore;
    requires transitive org.apache.httpcomponents.httpmime;

    requires transitive com.google.common;
    requires transitive com.google.gson;
    requires transitive org.apache.commons.lang3;

    exports consulo.httpClient.icon;
    exports consulo.httpClient.localize;
    exports org.javamaster.httpclient;
    exports org.javamaster.httpclient.highlighting.support;
    exports org.javamaster.httpclient.map;
    exports org.javamaster.httpclient.parser;
    exports org.javamaster.httpclient.psi;
    exports org.javamaster.httpclient.psi.impl;
    exports org.javamaster.httpclient.highlighting;
    exports org.javamaster.httpclient.ui;
    exports consulo.httpClient;
    exports org.javamaster.httpclient.factory;
    exports org.javamaster.httpclient.inject;
    exports org.javamaster.httpclient.model;
    exports org.javamaster.httpclient.run;
    exports org.javamaster.httpclient.utils;
}