package org.javamaster.httpclient.impl.startup;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicImpl;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.event.FileEditorManagerListener;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.HttpFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Add a top toolbar to the http file
 *
 * @author yudong
 */
@TopicImpl(ComponentScope.APPLICATION)
public class HttpPostStartupActivity implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (!(file.getFileType() instanceof HttpFileType)) {
            return;
        }

        FileEditor fileEditor = source.getSelectedEditor(file);
        if (fileEditor == null) {
            return;
        }

        // TODO !
//        Application.get().executeOnPooledThread(() -> {
//            Project project = source.getProject();
//            Module module = ModuleUtilCore.findModuleForFile(file, project);
//            FileTypeManager fileTypeManagerEx = FileTypeManagerEx.getInstanceEx();
//
//            JsonFileType jsonFileType = JsonFileType.INSTANCE;
//            String jsonExtension = jsonFileType.getDefaultExtension();
//
//            Object extension = fileTypeManagerEx.getFileTypeByExtension(jsonExtension);
//            if (extension == jsonFileType) {
//                initTopForm(source, file, module, fileEditor);
//                return;
//            }
//
//            Application.get().invokeLater(() ->
//                            Application.get().runWriteAction(() -> {
//                                fileTypeManagerEx.associateExtension(jsonFileType, jsonExtension);
//                                System.out.println("The json suffix file has been associated with the " + jsonFileType);
//
//                                initTopForm(source, file, module, fileEditor);
//                            }),
//                    ModalityState.nonModal()
//            );
//        });
    }

    // TODO !
//    private void initTopForm(
//            FileEditorManager source,
//            VirtualFile file,
//            @Nullable Module module,
//            FileEditor fileEditor
//    ) {
//        HttpBackground
//                .runInBackgroundReadActionAsync(() -> {
//                    EnvFileService envFileService = EnvFileService.getService(source.getProject());
//                    VirtualFile parent = file.getParent();
//                    if (parent == null) {
//                        return null;
//                    }
//
//                    String path = parent.getPath();
//                    return envFileService.getPresetEnvSet(path);
//                })
//                .finishOnUiThread(envSet -> {
//                    if (envSet == null) {
//                        return;
//                    }
//
//                    HttpEditorTopForm httpEditorTopForm = new HttpEditorTopForm(file, module, fileEditor);
//
//                    httpEditorTopForm.initEnvCombo(envSet);
//
//                    fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm);
//
//                    source.addTopComponent(fileEditor, httpEditorTopForm.getMainPanel());
//                })
//                .exceptionallyOnUiThread(throwable ->
//                        NotifyUtil.notifyError(source.getProject(), throwable.getMessage())
//                );
//    }
}
