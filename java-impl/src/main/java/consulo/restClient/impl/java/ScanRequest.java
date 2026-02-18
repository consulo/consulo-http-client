package consulo.restClient.impl.java;

import com.intellij.java.language.psi.PsiMethod;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.Project;
import consulo.restClient.impl.java.spring.SpringControllerScanService;
import consulo.util.dataholder.Key;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author yudong
 */
public class ScanRequest {
    private static final Map<String, Key<CachedValue<Map<String, List<Request>>>>> keyMap = new ConcurrentHashMap<>();

    public static PsiMethod findApiMethod(Module module, String searchTxt, String method) {
        Map<String, List<Request>> requestMap = getCacheRequestMap(module, module.getProject());

        List<Request> requests = requestMap.get(searchTxt + "-" + method);
        if (requests == null) {
            return null;
        }

        // There may be more than one controller method here, so for simplicity, take the first one directly,
        // without making complex judgments based on the mapping rules of SpringMVC
        Request request = requests.get(0);

        return request.getPsiElement();
    }

    public static void fetchRequests(Project project, GlobalSearchScope searchScope, Consumer<Request> consumer) {
        SpringControllerScanService controllerScanService = SpringControllerScanService.getService(project);

        controllerScanService.fetchRequests(project, searchScope, consumer);
    }

    public static Map<String, List<Request>> getCacheRequestMap(Module module, Project project) {
        SpringControllerScanService controllerScanService = SpringControllerScanService.getService(project);

        Key<CachedValue<Map<String, List<Request>>>> key = keyMap.computeIfAbsent(module.getName(),
            it -> Key.create("httpClient.requestMap." + it));

        return CachedValuesManager.getManager(project)
            .getCachedValue(module, key, () -> {
                List<Request> requests = controllerScanService.findRequests(project, module.getModuleWithLibrariesScope());

                Map<String, List<Request>> requestMap = requests.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Request::toString));

                return CachedValueProvider.Result.create(requestMap, PsiModificationTracker.MODIFICATION_COUNT);
            }, false);
    }
}
