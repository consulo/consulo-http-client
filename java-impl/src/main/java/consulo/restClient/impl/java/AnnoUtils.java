package consulo.httpClient.impl.java;


import com.intellij.java.language.jvm.annotation.*;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiMethod;

import java.lang.reflect.Field;
import java.util.*;

public class AnnoUtils {
    private static final Set<String> javaMetaAnnoSet = Set.of(
        "java.lang.annotation.Target",
        "java.lang.annotation.Documented",
        "java.lang.annotation.Retention"
    );

    public static Object getAttributeValue(JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }

        if (attributeValue instanceof JvmAnnotationConstantValue) {
            return ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
        }

        if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        }

        if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<JvmAnnotationAttributeValue> values = ((JvmAnnotationArrayValue) attributeValue).getValues();
            List<Object> list = new ArrayList<>(values.size());
            for (JvmAnnotationAttributeValue value : values) {
                Object res = getAttributeValue(value);
                if (res != null) {
                    list.add(res);
                } else {
                    // 如果是jar包里的JvmAnnotationConstantValue则无法正常获取值
                    try {
                        Class<? extends JvmAnnotationAttributeValue> clazz = value.getClass();

                        Field myElement = clazz.getSuperclass().getDeclaredField("myElement");
                        myElement.setAccessible(true);

                        Object elObj = myElement.get(value);

                        if (elObj instanceof PsiExpression) {
                            list.add(((PsiExpression) elObj).getText());
                        }
                    } catch (Exception e) {
                        System.err.println(e.getClass().getSimpleName());
                    }
                }
            }
            return list;
        }

        if (attributeValue instanceof JvmAnnotationClassValue) {
            return ((JvmAnnotationClassValue) attributeValue).getQualifiedName();
        }

        return null;
    }

    public static PsiAnnotation getClassAnnotation(PsiClass psiClass, String... annoNames) {
        if (annoNames.length == 0) {
            return null;
        }

        PsiAnnotation annotation;
        for (String name : annoNames) {
            annotation = psiClass.getAnnotation(name);
            if (annotation != null) {
                return annotation;
            }
        }

        List<PsiClass> classes = new ArrayList<>();

        classes.add(psiClass.getSuperClass());

        classes.addAll(Arrays.asList(psiClass.getInterfaces()));

        for (PsiClass superPsiClass : classes) {
            if (superPsiClass == null) {
                continue;
            }

            PsiAnnotation classAnnotation = getClassAnnotation(superPsiClass, annoNames);
            if (classAnnotation != null) {
                return classAnnotation;
            }
        }

        return null;
    }

    public static List<PsiAnnotation> collectMethodAnnotations(PsiMethod psiMethod) {
        Set<PsiAnnotation> annotations = new LinkedHashSet<>();

        annotations.addAll(Arrays.asList(psiMethod.getModifierList().getAnnotations()));

        for (PsiMethod superMethod : psiMethod.findSuperMethods()) {
            annotations.addAll(collectMethodAnnotations(superMethod));
        }

        return new ArrayList<>(annotations);
    }

    public static PsiAnnotation getQualifiedAnnotation(PsiAnnotation psiAnnotation, String qualifiedName) {
        String annotationQualifiedName = psiAnnotation != null ? psiAnnotation.getQualifiedName() : null;
        if (annotationQualifiedName == null) {
            return null;
        }

        if (qualifiedName.equals(annotationQualifiedName)) {
            return psiAnnotation;
        }

        if (javaMetaAnnoSet.contains(annotationQualifiedName)) {
            return null;
        }

        var element = psiAnnotation.getNameReferenceElement();
        if (element == null) {
            return null;
        }

        var resolve = element.resolve();
        if (!(resolve instanceof PsiClass)) {
            return null;
        }

        PsiClass psiClass = (PsiClass) resolve;

        if (!psiClass.isAnnotationType()) {
            return null;
        }

        PsiAnnotation annotation = psiClass.getAnnotation(qualifiedName);
        if (annotation != null && qualifiedName.equals(annotation.getQualifiedName())) {
            return annotation;
        }

        for (PsiAnnotation classAnnotation : psiClass.getAnnotations()) {
            PsiAnnotation qualifiedAnnotation = getQualifiedAnnotation(classAnnotation, qualifiedName);

            if (qualifiedAnnotation != null) {
                return qualifiedAnnotation;
            }
        }

        return null;
    }

    public static Object findAnnotationValue(JvmAnnotationAttribute attribute, String... attrNames) {
        if (attrNames.length == 0) {
            return null;
        }

        String attributeName = attribute.getAttributeName();
        boolean matchAttrName = false;
        for (String attrName : attrNames) {
            if (attributeName.equals(attrName)) {
                matchAttrName = true;
                break;
            }
        }

        if (!matchAttrName) {
            return null;
        }

        JvmAnnotationAttributeValue attributeValue = attribute.getAttributeValue();

        return findAttributeValue(attributeValue);
    }

    private static Object findAttributeValue(JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }

        if (attributeValue instanceof JvmAnnotationConstantValue) {
            Object constantValue = ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
            return constantValue != null ? constantValue.toString() : null;
        }

        if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        }

        if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<String> values = new ArrayList<>();
            for (JvmAnnotationAttributeValue value : ((JvmAnnotationArrayValue) attributeValue).getValues()) {
                values.add((String) findAttributeValue(value));
            }
            return values;
        }

        return null;
    }
}
