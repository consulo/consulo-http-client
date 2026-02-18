package consulo.restClient.impl.java;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.javadoc.PsiDocComment;
import consulo.language.psi.PsiElement;
import org.javamaster.httpclient.psi.HttpPsiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-01-20
 */
public class JavaHttpUtils {
    public static final String REQUEST_BODY_ANNO_NAME = "org.springframework.web.bind.annotation.RequestBody";
    public static final String API_OPERATION_ANNO_NAME = "io.swagger.annotations.ApiOperation";
    public static final String API_MODEL_PROPERTY_ANNO_NAME = "io.swagger.annotations.ApiModelProperty";


    public static String generateAnno(PsiAnnotation annotation) {
        String html = "<div class='definition'>\n" +
            "    <span style=\"color:#808000;\">@</span>" +
            "<a href=\"psi_element://" + annotation.getQualifiedName() + "\">" +
            "<span style=\"color:#808000;\">" +
            (annotation.getNameReferenceElement() != null ? annotation.getNameReferenceElement().getText() : "") +
            "</span></a>" +
            "<span>" + annotation.getParameterList().getText() + "</span>\n" +
            "</div>";

        return html;
    }

    public static String getMethodDesc(PsiMethod psiMethod) {
        List<String> list = new ArrayList<>();

        PsiDocComment docComment = psiMethod.getDocComment();
        if (docComment != null) {
            PsiElement commentData = HttpPsiUtils.getNextSiblingByType(
                docComment.getFirstChild(),
                JavaDocTokenType.DOC_COMMENT_DATA,
                false
            );

            String comment = commentData != null ? commentData.getText().trim() : null;

            if (comment != null) {
                list.add(comment);
            }
        }

        PsiAnnotation annotation = psiMethod.getAnnotation(API_OPERATION_ANNO_NAME);
        if (annotation != null) {
            PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue("value");
            if (attributeValue instanceof PsiLiteralExpression) {
                Object value = ((PsiLiteralExpression) attributeValue).getValue();
                String desc = value != null ? value.toString().trim() : null;

                if (desc != null) {
                    list.add(desc);
                }
            }
        }

        return String.join(" ", list);
    }
}
