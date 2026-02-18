package consulo.restClient.impl.java.inlay;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.codeEditor.Editor;
import consulo.codeEditor.event.EditorMouseEvent;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.inlay.InlayActionHandler;
import consulo.language.editor.inlay.InlayActionPayload;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.restClient.impl.java.JavaHttpUtils;
import consulo.restClient.impl.java.PsiUtils;
import consulo.restClient.impl.java.Request;
import consulo.restClient.impl.java.ScanRequest;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.model.HttpMethod;
import org.javamaster.httpclient.utils.HttpUtilsPart;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpInlayActionHandler implements InlayActionHandler {

    @Nonnull
    @Override
    public String getHandlerId() {
        return "HttpInlayHintsCollector";
    }

    @Deprecated
    @Override
    public void handleClick(@NotNull Editor editor, @NotNull InlayActionPayload payload) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleClick(@NotNull EditorMouseEvent e, @NotNull InlayActionPayload payload) {
        InlayActionPayload.PsiPointerInlayActionPayload actionPayload = (InlayActionPayload.PsiPointerInlayActionPayload) payload;
        PsiLiteralExpression element = (PsiLiteralExpression) actionPayload.getPointer().getElement();
        if (element == null) {
            return;
        }

        Project project = element.getProject();

        new Task.Backgroundable(project, NlsBundle.message("creating.file"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                Application.get().runReadAction(() -> {
                    PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

                    Module module = ModuleUtilCore.findModuleForPsiElement(element);
                    if (module == null) {
                        return;
                    }

                    Map<String, List<Request>> map = ScanRequest.getCacheRequestMap(module, project);

                    for (List<Request> value : map.values()) {
                        for (Request request : value) {
                            PsiMethod psiMethod = request.getPsiElement();
                            if (psiMethod == null) {
                                continue;
                            }

                            if (method != psiMethod) {
                                continue;
                            }

                            Application.get().invokeLater(() ->
                                Application.get().runWriteAction(() ->
                                    createRequest(project, request)
                                )
                            );

                            return;
                        }
                    }
                });
            }
        }.queue();
    }

    private void createRequest(Project project, Request request) {
        PsiMethod psiMethod = request.getPsiElement();
        if (psiMethod == null) {
            return;
        }

        String methodDesc = JavaHttpUtils.getMethodDesc(psiMethod);
        HttpMethod httpMethod = request.getMethod();
        LightVirtualFile lightVirtualFile = new LightVirtualFile("TemporaryHttpFile.http");

        Pair pair = generateBody(psiMethod);

        String contentType = pair.first;
        String body = pair.second;

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            String content = "### " + methodDesc + "\n" +
                    httpMethod.name() + " http://localhost" + request.getPath() + "\n" +
                    "Accept: application/json\n" +
                    "Content-Type: " + contentType + "\n\n" +
                    body;

            try {
                lightVirtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String queryPart = body.isEmpty() ? "" : "?" + body;

            String content = "### " + methodDesc + "\n" +
                    httpMethod.name() + " http://localhost" + request.getPath() + queryPart + "\n" +
                    "Accept: application/json\n" +
                    "Content-Type: " + contentType + "\n\n";

            try {
                lightVirtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileEditorManager.getInstance(project).openFile(lightVirtualFile, true);
    }

    private Pair generateBody(PsiMethod psiMethod) {
        String body = "";
        boolean hasAnno = false;

        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            if (parameter.getAnnotation(JavaHttpUtils.REQUEST_BODY_ANNO_NAME) == null) {
                continue;
            }

            hasAnno = true;
            PsiClass psiClass = PsiUtils.resolvePsiType(parameter.getType());
            if (psiClass == null) {
                continue;
            }

            Map<String, String> map = new LinkedHashMap<>();
            for (PsiField field : psiClass.getFields()) {
                PsiModifierList modifierList = field.getModifierList();
                if (modifierList != null && modifierList.hasModifierProperty("static")) {
                    continue;
                }

                map.put(field.getName(), "");
            }

            PsiClass superClass = psiClass.getSuperClass();
            if (superClass != null && superClass.getQualifiedName() != null &&
                !superClass.getQualifiedName().startsWith("java")) {
                for (PsiField field : superClass.getFields()) {
                    PsiModifierList modifierList = field.getModifierList();
                    if (modifierList != null && modifierList.hasModifierProperty("static")) {
                        continue;
                    }

                    map.put(field.getName(), "");
                }
            }

            body = HttpUtilsPart.gson.toJson(map);
            break;
        }

        if (hasAnno) {
            return new Pair("application/json", body);
        }

        List<String> list = new ArrayList<>();
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            String name = parameter.getName();
            PsiClass psiClass = PsiUtils.resolvePsiType(parameter.getType());
            if (psiClass != null && psiClass.getQualifiedName() != null &&
                psiClass.getQualifiedName().startsWith("java")) {
                list.add(name + "=");
            } else if (psiClass != null) {
                for (PsiField field : psiClass.getFields()) {
                    PsiModifierList modifierList = field.getModifierList();
                    if (modifierList != null && modifierList.hasModifierProperty("static")) {
                        continue;
                    }

                    list.add(field.getName() + "=");
                }

                PsiClass superClass = psiClass.getSuperClass();
                if (superClass != null && superClass.getQualifiedName() != null &&
                    !superClass.getQualifiedName().startsWith("java")) {
                    for (PsiField field : superClass.getFields()) {
                        PsiModifierList modifierList = field.getModifierList();
                        if (modifierList != null && modifierList.hasModifierProperty("static")) {
                            continue;
                        }

                        list.add(field.getName() + "=");
                    }
                }
            }
        }

        body = list.isEmpty() ? "" : String.join("&", list);

        return new Pair("application/x-www-form-urlencoded", body);
    }

    private static class Pair {
        final String first;
        final String second;

        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
