package org.javamaster.httpclient.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.model.HttpRequestEnum;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.psi.HttpComment;
import org.javamaster.httpclient.psi.HttpMethod;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.psi.HttpRequestTarget;
import org.javamaster.httpclient.run.HttpRunConfigurationApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-01-19
 */
public class HttpUtilsPart {
    public static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .registerTypeAdapter(Date.class, new DateTypeAdapter())
        .create();

    public static final String HTTP_TYPE_ID = "intellijHttpClient";

    public static final long READ_TIMEOUT = 3600L;
    public static final long CONNECT_TIMEOUT = 30L;
    public static final int TIMEOUT = 10_000;

    public static final String CR_LF = "\r\n";

    public static boolean isFileInIdeaDir(VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.getName().startsWith("tmp");
    }

    public static Image pickMethodIcon(String method) {
        try {
            HttpRequestEnum methodType = HttpRequestEnum.getInstance(method);

            return methodType.getIcon();
        }
        catch (UnsupportedOperationException ignored) {
            return HttpIcons.FILE;
        }
    }

    public static Module getOriginalModule(HttpRequestTarget requestTarget) {
        Project project = requestTarget.getProject();

        VirtualFile virtualFile = getOriginalFile(requestTarget);
        if (virtualFile == null) {
            return null;
        }

        return ModuleUtilCore.findModuleForFile(virtualFile, project);
    }

    public static VirtualFile getOriginalFile(HttpRequestTarget requestTarget) {
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(requestTarget);
        if (!isFileInIdeaDir(virtualFile)) {
            return virtualFile;
        }

        HttpMethod httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod.class);
        if (httpMethod == null) {
            return null;
        }

        String tabName = getTabName(httpMethod);

        return getOriginalFile(requestTarget.getProject(), tabName);
    }

    public static String getTabName(HttpMethod httpMethod) {
        HttpRequestBlock requestBlock = PsiTreeUtil.getParentOfType(httpMethod, HttpRequestBlock.class);
        if (requestBlock == null) {
            return "HTTP Request ▏#0";
        }

        HttpComment comment = requestBlock.getComment();
        if (comment != null) {
            String text = comment.getText();
            String tabName = text.substring(3).trim();
            if (!tabName.isEmpty()) {
                return tabName;
            }
        }

        HttpFile httpFile = (HttpFile) requestBlock.getParent();
        List<HttpRequestBlock> requestBlocks = httpFile.getRequestBlocks();

        for (int index = 0; index < requestBlocks.size(); index++) {
            if (requestBlock.equals(requestBlocks.get(index))) {
                return "HTTP Request ▏#" + (index + 1);
            }
        }

        return "HTTP Request ▏#0";
    }

    public static VirtualFile getOriginalFile(Project project, String tabName) {
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings configurationSettings = runManager.getAllSettings()
            .stream()
            .filter(it -> it.getConfiguration() instanceof HttpRunConfigurationApi
                && it.getConfiguration().getName().equals(tabName))
            .findFirst()
            .orElse(null);

        if (configurationSettings == null) {
            return null;
        }

        HttpRunConfigurationApi httpRunConfiguration = (HttpRunConfigurationApi) configurationSettings.getConfiguration();

        return VirtualFileUtil.findFileByIoFile(new File(httpRunConfiguration.getHttpFilePath()), true);
    }

    public static String constructFilePath(String filePath, String parentPath) {
        if (filePath.startsWith("/") || (filePath.length() > 1 && filePath.charAt(1) == ':')) {
            // 绝对路径
            return filePath;
        }
        else {
            return parentPath + "/" + filePath;
        }
    }

    public static PsiElement resolveFilePath(String path, String httpFileParentPath, Project project) {
        String filePath = constructFilePath(path, httpFileParentPath);

        File file = new File(filePath);
        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, false);
        if (virtualFile == null) {
            return null;
        }

        if (virtualFile.isDirectory()) {
            return PsiManager.getInstance(project).findDirectory(virtualFile);
        }

        return PsiUtilCore.getPsiFile(project, virtualFile);
    }

    public static List<String> getReqBodyDesc(Object reqBody) {
        int maxSizeLimit = 50000;
        List<String> descList = new ArrayList<>();

        if (reqBody instanceof String) {
            String str = (String) reqBody;
            if (str.length() > maxSizeLimit) {
                descList.add(
                    str.substring(0, maxSizeLimit) + CR_LF + "......(" + NlsBundle.message("content.truncated") + ")"
                );
            }
            else {
                descList.add(str);
            }
        }
        else if (reqBody instanceof Pair) {
            @SuppressWarnings("unchecked")
            Pair<byte[], String> pair = (Pair<byte[], String>) reqBody;

            descList.add(pair.second);
        }
        else if (reqBody instanceof List) {
            @SuppressWarnings("unchecked")
            List<Pair<byte[], String>> list = (List<Pair<byte[], String>>) reqBody;

            list.forEach(it -> {
                String desc = it.second;

                String bodyDesc = desc.length() > maxSizeLimit
                    ? desc + CR_LF + "......(" + NlsBundle.message("content.truncated") + ")" + CR_LF
                    : desc;

                descList.add(bodyDesc);
            });
        }

        return descList;
    }

    public static String getVersionDesc(consulo.http.HttpVersion version) {
        return version == consulo.http.HttpVersion.HTTP_1_1 ? "HTTP/1.1" : "HTTP/2";
    }

//    public static Pair<HttpRequest.BodyPublisher, Long> convertToReqBodyPublisher(Object reqBody) {
//        if (reqBody == null) {
//            return new Pair<>(HttpRequest.BodyPublishers.noBody(), 0L);
//        }
//
//        long multipartLength = 0L;
//        HttpRequest.BodyPublisher bodyPublisher;
//
//        if (reqBody instanceof String) {
//            bodyPublisher = HttpRequest.BodyPublishers.ofString((String) reqBody);
//        }
//        else if (reqBody instanceof Pair) {
//            @SuppressWarnings("unchecked")
//            Pair<byte[], String> pair = (Pair<byte[], String>) reqBody;
//
//            bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(pair.first);
//        }
//        else if (reqBody instanceof List) {
//            @SuppressWarnings("unchecked")
//            List<Pair<byte[], String>> list = (List<Pair<byte[], String>>) reqBody;
//
//            List<byte[]> byteArrays = list.stream().map(it -> it.first).collect(Collectors.toList());
//
//            bodyPublisher = HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
//
//            multipartLength = byteArrays.stream().mapToLong(it -> it.length).sum();
//        }
//        else {
//            System.err.println(NlsBundle.message("reqBody.unknown", reqBody.getClass().toString()));
//
//            bodyPublisher = HttpRequest.BodyPublishers.noBody();
//        }
//
//        return new Pair<>(bodyPublisher, multipartLength);
//    }
}
