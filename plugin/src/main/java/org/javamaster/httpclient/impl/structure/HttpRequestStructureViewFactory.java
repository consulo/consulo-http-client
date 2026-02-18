package org.javamaster.httpclient.impl.structure;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.StructureViewModel.ElementInfoProvider;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
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

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
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
