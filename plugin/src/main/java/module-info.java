/**
 * @author VISTALL
 * @since 2025-07-21
 */
module consulo.rest.client {
    requires consulo.rest.client.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.execution.api;
    requires consulo.diff.api;
    requires consulo.http.api;

    //   requires consulo.json.api;

    requires consulo.language.editor.refactoring.api;
    requires consulo.language.impl;

    requires forms.rt;

//    requires org.mozilla.rhino;
//    requires json.path;

    requires org.apache.commons.compress;
    requires org.apache.commons.io;

    requires com.google.gson;
    requires org.apache.commons.lang3;
}                                                                               