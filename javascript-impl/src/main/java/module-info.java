/**
 * Optional integration with the JavaScript plugin: JavaScript inside handler scripts ({@code {% %}} blocks)
 * and navigation from paths in script strings. Active only when consulo.javascript is installed.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
module consulo.http.client.javascript.impl {
    requires consulo.rest.client.api;
    requires consulo.javascript.base.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.code.editor.api;
    requires consulo.project.api;

    exports consulo.httpClient.impl.javascript;

    // http-client-api.js
    opens consulo.httpClient.impl.javascript;
}
