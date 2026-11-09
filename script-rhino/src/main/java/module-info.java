/**
 * Handler script engine based on Mozilla Rhino. Runs in interpreted mode and talks to Java only through
 * plain function objects (no reflection), so it also works in a GraalVM native image.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
module consulo.http.client.script.rhino {
    requires consulo.rest.client.api;
    requires org.mozilla.rhino;
    requires com.google.gson;
    requires java.xml;

    exports consulo.httpClient.script.rhino;
}
