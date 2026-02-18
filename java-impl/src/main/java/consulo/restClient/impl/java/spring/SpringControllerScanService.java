package consulo.restClient.impl.java.spring;

import com.intellij.java.indexing.impl.search.JavaSourceFilterScope;
import com.intellij.java.indexing.impl.stubs.index.JavaAnnotationIndex;
import com.intellij.java.language.psi.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.project.Project;
import consulo.restClient.impl.java.AnnoUtils;
import org.javamaster.httpclient.model.HttpMethod;
import consulo.restClient.impl.java.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
public class SpringControllerScanService {

    public List<Request> findRequests(Project project, GlobalSearchScope searchScope) {
        List<Request> requests = new ArrayList<>();

        fetchRequests(project, searchScope, requests::add);

        return requests;
    }

    public void fetchRequests(Project project, GlobalSearchScope scope, Consumer<Request> consumer) {
        JavaAnnotationIndex annotationIndex = JavaAnnotationIndex.getInstance();

        Collection<PsiAnnotation> annotations = StubIndex.getElements(
            annotationIndex.getKey(), Control.Controller.getSimpleName(), project, new JavaSourceFilterScope(scope),
            PsiAnnotation.class
        );

        iterateControllers(annotations, consumer);

        Collection<PsiAnnotation> annotationsRest = StubIndex.getElements(
            annotationIndex.getKey(), Control.RestController.getSimpleName(), project, new JavaSourceFilterScope(scope),
            PsiAnnotation.class
        );

        iterateControllers(annotationsRest, consumer);
    }

    private void iterateControllers(Collection<PsiAnnotation> controllerAnnoList, Consumer<Request> consumer) {
        for (PsiAnnotation controllerAnno : controllerAnnoList) {
            PsiModifierList psiModifierList = (PsiModifierList) controllerAnno.getParent();
            PsiClass controllerClass = (PsiClass) psiModifierList.getParent();
            if (controllerClass == null) {
                continue;
            }

            PsiAnnotation psiAnnotation = AnnoUtils.getClassAnnotation(
                controllerClass,
                SpringHttpMethod.REQUEST_MAPPING.getShortName(),
                SpringHttpMethod.REQUEST_MAPPING.getQualifiedName()
            );

            List<Request> childrenRequests = new ArrayList<>();
            List<Request> parentRequests = new ArrayList<>();

            if (psiAnnotation != null) {
                parentRequests = getRequests(psiAnnotation, null);
            }

            List<Request> requests = Arrays.stream(controllerClass.getAllMethods())
                .map(this::getRequests)
                .flatMap(List::stream)
                .collect(Collectors.toList());

            childrenRequests.addAll(requests);

            if (parentRequests.isEmpty()) {
                childrenRequests.forEach(consumer);
            } else {
                for (Request parentRequest : parentRequests) {
                    for (Request childRequest : childrenRequests) {
                        Request request = childRequest.copyWithParent(parentRequest);
                        consumer.accept(request);
                    }
                }
            }
        }
    }

    private List<Request> getRequests(PsiMethod method) {
        List<PsiAnnotation> methodAnnotations = AnnoUtils.collectMethodAnnotations(method);

        return methodAnnotations.stream()
            .map(it -> getRequests(it, method))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private List<Request> getRequests(PsiAnnotation annotation, PsiMethod psiMethod) {
        SpringHttpMethod httpMethod = SpringHttpMethod.getByQualifiedName(annotation.getQualifiedName());

        if (httpMethod == null) {
            PsiJavaCodeReferenceElement nameRef = annotation.getNameReferenceElement();
            httpMethod = SpringHttpMethod.getByShortName(nameRef != null ? nameRef.getText() : null);
        }

        Set<HttpMethod> methods = new HashSet<>();
        List<String> paths = new ArrayList<>();
        CustomRefAnnotation refAnnotation = null;

        if (httpMethod == null) {
            refAnnotation = findCustomAnnotation(annotation);
            if (refAnnotation == null) {
                return Collections.emptyList();
            }

            methods.addAll(refAnnotation.getMethods());
        } else {
            methods.add(httpMethod.getMethod());
        }

        boolean hasImplicitPath = true;
        PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
        for (PsiNameValuePair attribute : attributes) {
            String name = attribute.getAttributeName();

            if (methods.contains(HttpMethod.REQUEST) && "method".equals(name)) {
                Object value = AnnoUtils.getAttributeValue(attribute.getValue());
                if (value instanceof String) {
                    methods.add(HttpMethod.parse((String) value));
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item != null) {
                            String tmp = item.toString();
                            tmp = tmp.substring(tmp.lastIndexOf(".") + 1);
                            methods.add(HttpMethod.parse(tmp));
                        }
                    }
                }
            }

            boolean flag = false;
            for (String path : Arrays.asList("value", "path")) {
                if (path.equals(name)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                continue;
            }

            Object value = AnnoUtils.getAttributeValue(attribute.getValue());
            if (value instanceof String) {
                paths.add(formatPath(value));
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    paths.add(formatPath(item));
                }
            } else {
                System.err.println(String.format("Scan api: %s,Class: %s", value, value != null ? value.getClass() : null));
            }

            hasImplicitPath = false;
        }

        if (hasImplicitPath && psiMethod != null) {
            if (refAnnotation != null) {
                paths.addAll(refAnnotation.getPaths());
            } else {
                paths.add("/");
            }
        }

        return paths.stream()
            .flatMap(path -> methods.stream()
                .filter(method -> method != HttpMethod.REQUEST || methods.size() <= 1)
                .map(method -> new Request(method, path, psiMethod, null)))
            .collect(Collectors.toList());
    }

    private CustomRefAnnotation findCustomAnnotation(PsiAnnotation psiAnnotation) {
        PsiAnnotation qualifiedAnnotation = AnnoUtils.getQualifiedAnnotation(
            psiAnnotation,
            SpringHttpMethod.REQUEST_MAPPING.getQualifiedName()
        );

        if (qualifiedAnnotation == null) {
            return null;
        }

        CustomRefAnnotation otherAnnotation = new CustomRefAnnotation();

        for (PsiNameValuePair attribute : qualifiedAnnotation.getParameterList().getAttributes()) {
            Object methodValues = AnnoUtils.findAnnotationValue(attribute, "method");

            if (methodValues != null) {
                List<?> methods = methodValues instanceof List ? (List<?>) methodValues : Collections.singletonList(methodValues);
                if (methods.isEmpty()) {
                    continue;
                }

                for (Object method : methods) {
                    if (method == null) {
                        continue;
                    }

                    HttpMethod parseMethods = HttpMethod.parse(method);
                    otherAnnotation.addMethods(parseMethods);
                }
                continue;
            }

            Object pathValues = AnnoUtils.findAnnotationValue(attribute, "path", "value");

            if (pathValues != null) {
                List<?> paths = pathValues instanceof List ? (List<?>) pathValues : Collections.singletonList(pathValues);
                for (Object path : paths) {
                    if (path == null) {
                        continue;
                    }

                    otherAnnotation.addPath((String) path);
                }
            }
        }

        return otherAnnotation;
    }

    private String formatPath(Object path) {
        String slash = "/";
        if (path == null) {
            return slash;
        }

        String currPath = path instanceof String ? (String) path : path.toString();

        if (currPath.startsWith(slash)) {
            return currPath;
        }

        return slash + currPath;
    }

    public static SpringControllerScanService getService(Project project) {
        return project.getService(SpringControllerScanService.class);
    }
}
