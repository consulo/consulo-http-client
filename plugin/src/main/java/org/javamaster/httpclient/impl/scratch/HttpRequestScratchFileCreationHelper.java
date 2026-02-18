package org.javamaster.httpclient.impl.scratch;

import consulo.annotation.component.ExtensionImpl;
import consulo.dataContext.DataContext;
import consulo.language.Language;
import consulo.language.scratch.ScratchFileCreationHelper;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpLanguage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@ExtensionImpl
public class HttpRequestScratchFileCreationHelper extends ScratchFileCreationHelper {
    private final String templateName = "HTTP Scratch";

    @Override
    public boolean prepareText(Project project, Context context, DataContext dataContext) {
        if (context.language != HttpLanguage.INSTANCE || StringUtil.isNotEmpty(context.text)) {
            return false;
        }

        String file = createFileFromTemplate(project);
        context.text = file + "\n";
        context.caretOffset = context.text.length();
        return true;
    }

    private String createFileFromTemplate(Project project) {
        try {
            String path = project.getBasePath() + "/scratches/" + templateName + ".http";
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return HttpLanguage.INSTANCE;
    }
}
