package org.javamaster.httpclient.impl.structure;

import consulo.fileEditor.structureView.*;
import consulo.fileEditor.structureView.StructureViewModel.ElementInfoProvider;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpRequestStructureViewFactory implements PsiStructureViewFactory {

    @NotNull
    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                StructureViewTreeElement root = HttpRequestStructureViewElement.create(psiFile, "Http Request", null);
                return new HttpClientViewModel(psiFile, editor, root);
            }
        };
    }

    private static class HttpClientViewModel extends StructureViewModelBase implements ElementInfoProvider {
        public HttpClientViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull StructureViewTreeElement root) {
            super(psiFile, editor, root);
        }

        @Override
        public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
            return false;
        }

        @Override
        public boolean isAlwaysLeaf(StructureViewTreeElement element) {
            return false;
        }
    }
}
