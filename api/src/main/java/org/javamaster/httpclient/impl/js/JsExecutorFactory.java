package org.javamaster.httpclient.impl.js;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import org.javamaster.httpclient.parser.HttpFile;

/**
 * Engine for handler scripts ({@code <! {% %}}, {@code < {% %}}, {@code > {% %}}).
 * The first registered implementation is used; without any, scripts are skipped.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface JsExecutorFactory {
    /**
     * Short engine id, for example {@code rhino}.
     */
    String getId();

    /**
     * Creates an executor for one request run. {@code client.global} values must survive between executors
     * of the same factory.
     */
    JsExecutor create(Project project, HttpFile httpFile, LocalizeValue tabName);
}
