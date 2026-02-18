package org.javamaster.httpclient.impl.symbol;

import consulo.ide.impl.idea.ide.util.gotoByName.GotoSymbolModel2;
import consulo.disposer.Disposable;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

public class GotoApiModel2 extends GotoSymbolModel2 {
    public GotoApiModel2(@NotNull Project project, @NotNull Disposable disposable) {
        super(project, disposable);
    }
}
