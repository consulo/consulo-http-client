package org.javamaster.httpclient.impl.utils;

import consulo.document.FileDocumentManager;
import consulo.language.file.light.LightVirtualFile;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.enums.InnerVariableEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * @author yudong
 */
public class VirtualFileUtils {

    public static byte[] readNewestBytes(File file) throws IOException {
        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, false);
        if (virtualFile != null && isVirtualFileNewest(virtualFile, file)) {
            return readNewestBytes(virtualFile);
        }

        String absolutePath = file.getAbsoluteFile().toPath().normalize().toAbsolutePath().toString();
        if (!file.exists()) {
            throw new FileNotFoundException(absolutePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(NlsBundle.message("not.file", absolutePath));
        }

        return Files.readAllBytes(file.toPath());
    }

    private static byte[] readNewestBytes(VirtualFile virtualFile) throws IOException {
        if (virtualFile.isDirectory()) {
            throw new IllegalArgumentException(NlsBundle.message("not.file", virtualFile.getPath()));
        }

        var document = FileDocumentManager.getInstance().getCachedDocument(virtualFile);

        if (document != null) {
            return document.getText().getBytes();
        } else {
            return virtualFile.contentsToByteArray();
        }
    }

    public static String readNewestContent(File file) throws IOException {
        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, false);
        if (virtualFile != null && isVirtualFileNewest(virtualFile, file)) {
            return readNewestContent(virtualFile);
        }

        String absolutePath = file.getAbsoluteFile().toPath().normalize().toAbsolutePath().toString();
        if (!file.exists()) {
            throw new FileNotFoundException(absolutePath);
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(NlsBundle.message("not.file", absolutePath));
        }

        return Files.readString(file.toPath());
    }

    public static String readNewestContent(VirtualFile virtualFile) throws IOException {
        if (virtualFile.isDirectory()) {
            throw new IllegalArgumentException(NlsBundle.message("not.file", virtualFile.getPath()));
        }

        var document = FileDocumentManager.getInstance().getCachedDocument(virtualFile);

        if (document != null) {
            return document.getText();
        } else {
            return new String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8);
        }
    }

    public static File getDateHistoryDir(Project project) {
        Date date = new Date();

        String historyFolder = InnerVariableEnum.HISTORY_FOLDER.exec("", project);
        String dayStr = DateFormatUtils.format(date, "MM-dd");

        File parentDir = new File(historyFolder, dayStr);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        return parentDir;
    }

    public static VirtualFile createHttpVirtualFileFromText(
        byte[] txtBytes,
        String suffix,
        Project project,
        String tabName,
        boolean noLog
    ) throws IOException {
        File parentDir = getDateHistoryDir(project);

        String str = StringUtils.defaultString(tabName) + "-" + DateFormatUtils.format(new Date(), "hhmmss");
        Path path = Path.of(parentDir.toString(), "tmp-" + str + "." + suffix);

        if (noLog) {
            LightVirtualFile lightVirtualFile = new LightVirtualFile(path.toFile().getName());
            lightVirtualFile.setCharset(StandardCharsets.UTF_8);
            lightVirtualFile.setBinaryContent(txtBytes);
            return lightVirtualFile;
        }

        File file;
        if (Files.exists(path)) {
            file = path.toFile();
        } else {
            Path tempFile = Files.createFile(path);
            file = tempFile.toFile();
        }

        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, true);
        virtualFile.setBinaryContent(txtBytes);

        return virtualFile;
    }

    public static VirtualFile createHistoryHttpVirtualFile(
        String content,
        Project project,
        String tabName
    ) throws IOException {
        File parentDir = getDateHistoryDir(project);

        Path path = Path.of(parentDir.toString(), tabName + "-history.http");

        File file;
        if (Files.exists(path)) {
            file = path.toFile();
        } else {
            Path tempFile = Files.createFile(path);
            file = tempFile.toFile();
        }

        VirtualFile virtualFile = VirtualFileUtil.findFileByIoFile(file, true);
        virtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));

        return virtualFile;
    }

    private static boolean isVirtualFileNewest(VirtualFile virtualFile, File file) throws IOException {
        if (!file.exists()) {
            return false;
        }

        long timeStamp = virtualFile.getTimeStamp();
        long millis = Files.getLastModifiedTime(file.toPath()).toMillis();
        return timeStamp >= millis;
    }
}
