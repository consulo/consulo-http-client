package org.javamaster.httpclient.impl.action.addHttp;

import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.impl.TemplateSettings;
import consulo.ide.impl.idea.ide.impl.ProjectUtil;
import consulo.ui.ex.action.ActionUpdateThread;
import consulo.ui.ex.action.AnAction;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.impl.env.EnvFileService;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.ui.HttpEditorTopForm;
import org.javamaster.httpclient.impl.utils.NotifyUtil;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

/**
 * @author yudong
 */
public abstract class AddAction extends AnAction {

    public AddAction(String name) {
        super(name);
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    protected void startLiveTemplate(String abbreviation) {
        Project project = ProjectUtil.getActiveProject();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        Document document = editor.getDocument();

        if (!document.isWritable()) {
            return;
        }

        getApplication().runWriteAction(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(document.getTextLength(), "\n");

                editor.getCaretModel().moveToOffset(document.getTextLength());

                TemplateManager templateManager = TemplateManager.getInstance(project);
                var template = TemplateSettings.getInstance().getTemplate(abbreviation, "HTTP Request");
                templateManager.startTemplate(editor, template);
            });
        });
    }

    public static void createAndReInitEnvCompo(boolean isPrivate) {
        Project project = ProjectUtil.getActiveProject();

        String envFileName = isPrivate ? EnvFileService.PRIVATE_ENV_FILE_NAME : EnvFileService.ENV_FILE_NAME;

        VirtualFile envFile = EnvFileService.Companion.createEnvFile(envFileName, isPrivate, project);
        if (envFile == null) {
            NotifyUtil.notifyWarn(project, envFileName + " " + NlsBundle.message("file.exists"));
            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(envFile, true);

        NotifyUtil.notifyInfo(project, NlsBundle.message("file.created") + " " + envFileName);

        try {
            var allEditors = fileEditorManager.getAllEditors();
            for (var editor : allEditors) {
                HttpEditorTopForm httpEditorTopForm = editor.getUserData(HttpEditorTopForm.KEY);
                if (httpEditorTopForm == null) continue;

                Set<String> set = new LinkedHashSet<>();
                set.add("dev");
                set.add("uat");
                set.add("pro");
                httpEditorTopForm.initEnvCombo(set);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
