package consulo.httpClient.impl.java.dubbo;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.container.plugin.PluginId;
import consulo.project.Project;
import consulo.httpClient.impl.java.DubboClassLoader;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.impl.utils.NotifyUtil;
import org.javamaster.httpclient.utils.RandomStringUtils;
import org.javamaster.httpclient.utils.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yudong
 */
@SuppressWarnings("deprecation")
public class DubboJars {
    public static DubboClassLoader dubboClassLoader;

    private static boolean downloading = false;

    private static final String REPOSITORY_URL = "https://maven.aliyun.com/nexus/content/groups/public";

    private static final List<URL> jarUrls = new ArrayList<>();
    private static final Map<String, URL> jarMap = new LinkedHashMap<>();

    static {
        try {
            jarUrls.addAll(findPluginJarUrls());

            File dubboLibPath = getDubboLibPath();

            File[] listFiles = dubboLibPath.listFiles();

            if (listFiles != null) {
                for (File file : listFiles) {
                    jarUrls.add(file.toURI().toURL());
                }
            }

            dubboClassLoader = new DubboClassLoader(jarUrls.toArray(new URL[0]), DubboJars.class.getClassLoader());

            jarMap.put("javassist-3.30.2-GA.jar",
                    new URL(REPOSITORY_URL + "/org/javassist/javassist/3.30.2-GA/javassist-3.30.2-GA.jar"));
            jarMap.put("curator-client-4.0.1.jar",
                    new URL(REPOSITORY_URL + "/org/apache/curator/curator-client/4.0.1/curator-client-4.0.1.jar"));
            jarMap.put("curator-framework-4.0.1.jar",
                    new URL(REPOSITORY_URL + "/org/apache/curator/curator-framework/4.0.1/curator-framework-4.0.1.jar"));
            jarMap.put("netty-3.10.5.Final.jar",
                    new URL(REPOSITORY_URL + "/io/netty/netty/3.10.5.Final/netty-3.10.5.Final.jar"));
            jarMap.put("zookeeper-3.5.3-beta.jar",
                    new URL(REPOSITORY_URL + "/org/apache/zookeeper/zookeeper/3.5.3-beta/zookeeper-3.5.3-beta.jar"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean jarsNotDownloaded() {
        return jarUrls.size() != jarMap.size() + 2;
    }

    public static void downloadAsync(Project project) {
        if (downloading) {
            NotifyUtil.notifyCornerWarn(project, NlsBundle.message("download.not.finish"));
            return;
        }

        downloading = true;

        NotifyUtil.notifyCornerSuccess(project, NlsBundle.message("start.download"));

        new Task.Backgroundable(project, NlsBundle.message("dubbo.downloading"), true) {
            @Override
            public void run(ProgressIndicator indicator) {
                try {
                    File dubboLibPath = getDubboLibPath();

                    if (!dubboLibPath.exists()) {
                        dubboLibPath.mkdirs();
                    }

                    jarUrls.clear();

                    double faction = 1.0 / jarMap.size();

                    int index = 0;
                    for (Map.Entry<String, URL> entry : jarMap.entrySet()) {
                        String name = entry.getKey();
                        URL url = entry.getValue();

                        try (InputStream inputStream = url.openStream()) {
                            if (indicator.isCanceled()) {
                                throw new RuntimeException(NlsBundle.message("download.abort"));
                            }

                            File file = saveToFile(inputStream, name, dubboLibPath);

                            jarUrls.add(file.toURI().toURL());

                            indicator.setFraction((index + 1) * faction);

                            if (index != jarMap.size() - 1) {
                                TimeUnit.MILLISECONDS.sleep(1000 + RandomStringUtils.RANDOM.nextLong(1000));
                            }
                        }
                        index++;
                    }

                    dubboClassLoader.close();

                    jarUrls.addAll(findPluginJarUrls());

                    dubboClassLoader = new DubboClassLoader(jarUrls.toArray(new URL[0]), DubboRequest.class.getClassLoader());

                    com.intellij.util.ApplicationKt.getApplication().invokeLater(() -> {
                        NotifyUtil.notifyCornerSuccess(project, NlsBundle.message("dubbo.downloaded"));
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    com.intellij.util.ApplicationKt.getApplication().invokeLater(() -> {
                        NotifyUtil.notifyCornerWarn(
                                project,
                                NlsBundle.message("dubbo.downloaded.error") + " " + e.getMessage()
                        );
                    });
                } finally {
                    downloading = false;
                }
            }
        }.queue();
    }

    private static File saveToFile(InputStream inputStream, String name, File dubboLibPath) throws IOException {
        byte[] byteArray = StreamUtils.copyToByteArray(inputStream);

        File file = new File(dubboLibPath, name);

        if (file.exists()) {
            file.delete();
            System.out.println("deleted exists jar file: " + file);
        }

        Files.write(file.toPath(), byteArray);

        System.out.println("Downloaded dubbo jar " + name + " : " + file);

        return file;
    }

    private static List<URL> findPluginJarUrls() throws Exception {
        File dubboLibPath = getDubboLibPath();
        File libPath = dubboLibPath.getParentFile();
        File[] files = libPath.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        List<URL> urls = new ArrayList<>();
        for (File file : files) {
            if ((file.getName().contains("HttpRequest") && Files.size(file.toPath()) > 800000)
                    || file.getName().equals("dubbo-2.6.12.jar")) {
                urls.add(file.toURI().toURL());
            }
        }
        return urls;
    }

    private static File getDubboLibPath() {
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.findId("org.javamaster.HttpRequest"));
        File pluginPath = pluginDescriptor.getPluginPath().toFile();
        return new File(pluginPath, "lib/dubboLib");
    }
}
