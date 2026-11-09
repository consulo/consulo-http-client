package consulo.httpClient.script.rhino;

import consulo.annotation.component.ExtensionImpl;
import org.javamaster.httpclient.impl.js.JsExecutor;
import org.javamaster.httpclient.impl.js.JsExecutorFactory;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import org.javamaster.httpclient.parser.HttpFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-11-03
 */
@ExtensionImpl
public class RhinoJsExecutorFactory implements JsExecutorFactory {
    public static final String ID = "rhino";

    /**
     * Interpreted mode: no bytecode generation, required for GraalVM native image and cheap for short scripts.
     */
    static final ContextFactory CONTEXT_FACTORY = new ContextFactory() {
        @Override
        protected void onContextCreated(Context cx) {
            super.onContextCreated(cx);
            // interpreted mode: no bytecode generation
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_ES6);
        }
    };

    /**
     * {@code client.global} storage, lives as long as the application.
     */
    private final Map<String, String> myGlobalVariables = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public JsExecutor create(Project project, HttpFile httpFile, LocalizeValue tabName) {
        return new RhinoJsExecutor(httpFile, myGlobalVariables);
    }
}
