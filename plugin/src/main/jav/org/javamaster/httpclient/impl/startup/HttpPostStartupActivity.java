package org.javamaster.httpclient.impl.startup;

import consulo.application.Application;
import consulo.application.ModalityState;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.event.FileEditorManagerListener;
import consulo.json.JsonFileType;
import consulo.language.internal.FileTypeManagerEx;
import consulo.module.Module;
import consulo.module.ModuleUtil;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.javamaster.httpclient.HttpFileType;
import org.javamaster.httpclient.impl.background.HttpBackground;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add a top toolbar to the http file
 *
 * @author yudong
 */
public class HttpPostStartupActivity implements FileEditorManagerListener, ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile file : fileEditorManager.getOpenFiles()) {
            fileOpened(fileEditorManager, file);
        }

        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);

        return Unit.INSTANCE;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (!(file.getFileType() instanceof HttpFileType)) {
            return;
        }

        FileEditor fileEditor = source.getSelectedEditor(file);
        if (fileEditor == null) {
            System.err.println("Can't find file editor for " + file.getPath());
            return;
        }

        Application.get().executeOnPooledThread(() -> {
            Project project = source.getProject();
            Module module = ModuleUtil.findModuleForFile(file, project);
            FileTypeManagerEx fileTypeManagerEx = FileTypeManagerEx.getInstanceEx();

            JsonFileType jsonFileType = JsonFileType.INSTANCE;
            String jsonExtension = jsonFileType.getDefaultExtension();

            Object extension = fileTypeManagerEx.getFileTypeByExtension(jsonExtension);
            if (extension == jsonFileType) {
                initTopForm(source, file, module, fileEditor);
                return;
            }

            Application.get().invokeLater(() ->
                            Application.get().runWriteAction(() -> {
                                fileTypeManagerEx.associateExtension(jsonFileType, jsonExtension);
                                System.out.println("The json suffix file has been associated with the " + jsonFileType);

                                initTopForm(source, file, module, fileEditor);
                            }),
                    ModalityState.nonModal()
            );
        });
    }

    private void initTopForm(
            FileEditorManager source,
            VirtualFile file,
            @Nullable Module module,
            FileEditor fileEditor
    ) {
        HttpBackground
                .runInBackgroundReadActionAsync(() -> {
                    EnvFileService envFileService = EnvFileService.getService(source.getProject());
                    VirtualFile parent = file.getParent();
                    if (parent == null) {
                        return null;
                    }

                    String path = parent.getPath();
                    return envFileService.getPresetEnvSet(path);
                })
                .finishOnUiThread(envSet -> {
                    if (envSet == null) {
                        return;
                    }

                    HttpEditorTopForm httpEditorTopForm = new HttpEditorTopForm(file, module, fileEditor);

                    httpEditorTopForm.initEnvCombo(envSet);

                    fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm);

                    source.addTopComponent(fileEditor, httpEditorTopForm.getMainPanel());
                })
                .exceptionallyOnUiThread(throwable ->
                        NotifyUtil.notifyError(source.getProject(), throwable.getMessage())
                );
    }
}
