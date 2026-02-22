package org.javamaster.httpclient.impl.manipulator;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.factory.HttpPsiFactory;
import org.javamaster.httpclient.psi.HttpVariable;
import org.javamaster.httpclient.psi.HttpVariableName;
import org.jetbrains.annotations.Nullable;

/**
 * @author yudong
 */
@ExtensionImpl
public class HttpVariableNameManipulator extends AbstractElementManipulator<HttpVariableName> {

    @Nullable
    @Override
    public HttpVariableName handleContentChange(
            HttpVariableName element,
            TextRange range,
            String newContent
    ) {
        if (newContent == null || newContent.isBlank()) {
            return null;
        }

        HttpVariable variable = HttpPsiFactory.createVariable(element.getProject(), "GET {{" + newContent + "}}");

        element.getParent().replace(variable);

        return variable.getVariableName();
    }

    @Nonnull
    @Override
    public Class<HttpVariableName> getElementClass() {
        return HttpVariableName.class;
    }

}
