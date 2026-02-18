package consulo.restClient.impl.java;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 */
public class DubboClassLoader extends URLClassLoader {
    private final Set<String> needParentLoad;

    public DubboClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        needParentLoad = new HashSet<>();
        needParentLoad.add("org.javamaster.httpclient.map.LinkedMultiValueMap");
        needParentLoad.add("org.javamaster.httpclient.dubbo.DubboHandler");
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);

            if (c != null) {
                return c;
            }

            if (needParentLoad.contains(name)) {
                try {
                    c = getParent().loadClass(name);
                } catch (ClassNotFoundException e) {
                    c = findClass(name);
                }
            } else {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    c = getParent().loadClass(name);
                }
            }

            return c;
        }
    }
}
