package org.javamaster.httpclient.impl.dashboard.support;

import consulo.json.psi.JsonFile;
import consulo.json.psi.JsonObject;
import consulo.json.psi.JsonStringLiteral;
import consulo.application.Application;
import consulo.container.plugin.PluginId;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.light.LightVirtualFile;
import consulo.index.io.DigestUtil;
import org.javamaster.httpclient.model.PreJsFile;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.javamaster.httpclient.utils.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
public class JsTgz {
    private static boolean downloading = false;
    private static final Map<String, File> jsTgzFileMap = new HashMap<>();
    private static final Map<String, File> packageJsonMainJsFileMap = new HashMap<>();

    static {
        File jsLibPath = getJsLibPath();

        File[] listFiles = jsLibPath.listFiles();

        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    continue;
                }
                jsTgzFileMap.put(file.getName(), file);
            }
        }
    }

    public static List<PreJsFile> jsLibrariesNotDownloaded(List<PreJsFile> npmFiles) {
        return npmFiles.stream()
            .filter(it -> {
                String name = it.getUrlFile().getName();
                return jsTgzFileMap.get(name) == null;
            })
            .collect(Collectors.toList());
    }

    public static void initAndCacheNpmJsLibrariesFile(List<PreJsFile> npmFiles, Project project) {
        for (PreJsFile preJsFile : npmFiles) {
            File urlFile = preJsFile.getUrlFile();

            String name = urlFile.getName();
            String nameWithoutExtension = urlFile.getName().replaceFirst("[.][^.]+$", "");

            File file = packageJsonMainJsFileMap.get(name);
            if (file == null) {
                File tgzJsFile = jsTgzFileMap.get(name);

                File libDir = new File(tgzJsFile.getParentFile(), nameWithoutExtension);

                file = findPackageJsonMainJsFile(libDir, project);

                packageJsonMainJsFileMap.put(name, file);

                System.out.println("Cache the main js entry " + file + " of the " + name);
            }

            preJsFile.setFile(file);
        }
    }

    public static void initJsLibrariesVirtualFile(List<PreJsFile> preJsFiles) {
        for (PreJsFile preJsFile : preJsFiles) {
            VirtualFile virtualFile = VfsUtil.findFileByIoFile(preJsFile.getFile(), true);
            preJsFile.setVirtualFile(virtualFile);
        }
    }

    public static void downloadAsync(Project project, List<PreJsFile> npmFiles, @Nullable Runnable finished) {
        if (downloading) {
            NotifyUtil.notifyCornerWarn(project, NlsBundle.message("download.not.finish"));
            return;
        }

        downloading = true;

        NotifyUtil.notifyCornerSuccess(project, NlsBundle.message("js.downloading"));

        new Task.Backgroundable(project, NlsBundle.message("js.download"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    File jsLibPath = getJsLibPath();

                    if (!jsLibPath.exists()) {
                        jsLibPath.mkdirs();
                    }

                    double faction = 1.0 / npmFiles.size();

                    for (int index = 0; index < npmFiles.size(); index++) {
                        PreJsFile entry = npmFiles.get(index);
                        File npmFile = new File(entry.getUrl().toString());

                        URL url = entry.getUrl();

                        try (InputStream inputStream = url.openStream()) {
                            if (indicator.isCanceled()) {
                                throw new RuntimeException(NlsBundle.message("download.abort"));
                            }

                            saveAndExtract(inputStream, npmFile, jsLibPath);

                            indicator.setFraction((index + 1) * faction);

                            if (index != npmFiles.size() - 1) {
                                TimeUnit.MILLISECONDS.sleep(1000 + DigestUtil.random().nextLong(1000));
                            }
                        }
                    }

                    Application.get().invokeLater(() -> {
                        NotifyUtil.notifyCornerSuccess(project, NlsBundle.message("js.downloaded"));

                        if (finished != null) {
                            finished.run();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    Application.get().invokeLater(() -> {
                        NotifyUtil.notifyCornerError(
                            project,
                            NlsBundle.message("js.download.error") + " " + e
                        );
                    });
                } finally {
                    downloading = false;
                }
            }
        }.queue();
    }

    private static void saveAndExtract(InputStream inputStream, File npmFile, File jsLibPath) throws Exception {
        String name = npmFile.getName();
        String nameWithoutExtension = npmFile.getName().replaceFirst("[.][^.]+$", "");

        byte[] byteArray = StreamUtils.copyToByteArray(inputStream);

        File file = new File(jsLibPath, name);

        if (file.exists()) {
            file.delete();
            System.out.println("Deleted exists file: " + file);
        }

        Files.write(file.toPath(), byteArray);
        System.out.println("Downloaded js library " + file.getName() + " : " + file);

        File outputDir = new File(jsLibPath.getAbsolutePath(), nameWithoutExtension);

        if (outputDir.exists()) {
            deleteRecursively(outputDir);
            System.out.println("Deleted exists dir " + outputDir.getName() + ": " + outputDir);
        }

        outputDir.mkdirs();

        TgzExtractor.extract(file.getAbsolutePath(), outputDir.getAbsolutePath());

        System.out.println("Extracted js library " + file + " to " + outputDir);

        jsTgzFileMap.put(name, file);
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    private static File findPackageJsonMainJsFile(File libDir, Project project) {
        File packJson = new File(libDir, "package" + File.separator + "package.json");

        if (!packJson.exists()) {
            throw new IllegalArgumentException("Invalid library: " + libDir.getName());
        }

        String packageJsonStr;
        try {
            packageJsonStr = Files.readString(packJson.toPath());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid library: " + libDir.getName());
        }

        LightVirtualFile virtualFile = new LightVirtualFile("dummy.json", packageJsonStr);

        JsonFile jsonFile = (JsonFile) PsiUtil.getPsiFile(project, virtualFile);

        com.intellij.json.psi.JsonValue jsonValue = jsonFile.getTopLevelValue();
        if (!(jsonValue instanceof JsonObject)) {
            throw new IllegalArgumentException("Invalid library: " + libDir.getName());
        }

        JsonObject jsonObject = (JsonObject) jsonValue;

        com.intellij.json.psi.JsonProperty mainProperty = jsonObject.findProperty("main");
        if (mainProperty == null || !(mainProperty.getValue() instanceof JsonStringLiteral)) {
            throw new IllegalArgumentException("Invalid library: " + libDir.getName());
        }

        JsonStringLiteral jsonStringLiteral = (JsonStringLiteral) mainProperty.getValue();

        String entryJs = jsonStringLiteral.getValue();

        File file = new File(packJson.getParentFile(), entryJs);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("Invalid library: " + libDir.getName());
        }

        return file;
    }

    private static File getJsLibPath() {
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.findId("org.javamaster.HttpRequest"));
        File pluginPath = pluginDescriptor.getPluginPath().toFile();
        return new File(pluginPath, "lib/jsLib");
    }
}
