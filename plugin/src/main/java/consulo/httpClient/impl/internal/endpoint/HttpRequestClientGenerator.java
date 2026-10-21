package consulo.httpClient.impl.internal.endpoint;

import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.client.generator.ClientExample;
import consulo.endpoint.client.generator.ClientGenerator;
import consulo.endpoint.oas.OasEndpointPath;
import consulo.endpoint.oas.OasHttpMethod;
import consulo.endpoint.oas.OasOperation;
import consulo.endpoint.oas.OasParameter;
import consulo.endpoint.oas.OasParameterIn;
import consulo.endpoint.oas.OasRequestBody;
import consulo.endpoint.oas.OasResponse;
import consulo.endpoint.oas.OasSchema;
import consulo.endpoint.oas.OpenApiSpecification;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.localize.LocalizeValue;
import org.javamaster.httpclient.HttpFileType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class HttpRequestClientGenerator implements ClientGenerator {
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private static final String APPLICATION_JSON = "application/json";

    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{([^}:/]+)(?::[^}]*)?}");

    @Override
    public LocalizeValue getTitle() {
        return HttpClientLocalize.endpointClientGeneratorTitle();
    }

    @Override
    public @Nullable ClientExample generate(OpenApiSpecification openApiSpecification) {
        StringBuilder builder = new StringBuilder();
        for (OasEndpointPath path : openApiSpecification.getPaths()) {
            for (OasOperation operation : path.getOperations()) {
                appendRequest(builder, path, operation);
            }
        }
        if (builder.isEmpty()) {
            return null;
        }
        return ClientExample.fromFileExtension(builder.toString(), HttpFileType.DEFAULT_EXTENSION);
    }

    private static void appendRequest(StringBuilder builder, OasEndpointPath path, OasOperation operation) {
        OasHttpMethod httpMethod = operation.getMethod();
        String method = httpMethod.getMethodName().toUpperCase(Locale.ROOT);
        String absolutePath = path.getAbsolutePath();

        if (!builder.isEmpty()) {
            builder.append('\n');
        }

        String summary = operation.getSummary();
        builder.append("### ").append(summary != null && !summary.isBlank() ? summary : method + " " + absolutePath).append('\n');
        builder.append(method).append(' ').append(DEFAULT_BASE_URL).append(toTemplate(absolutePath));

        List<String> query = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        for (OasParameter parameter : operation.getParameters()) {
            if (parameter.getInPlace() == OasParameterIn.QUERY) {
                query.add(parameter.getName() + "={{" + parameter.getName() + "}}");
            }
            else if (parameter.getInPlace() == OasParameterIn.HEADER) {
                headers.add(parameter.getName() + ": {{" + parameter.getName() + "}}");
            }
        }
        if (!query.isEmpty()) {
            builder.append('?').append(String.join("&", query));
        }
        builder.append('\n');

        builder.append("Accept: ").append(findAccept(operation)).append('\n');
        for (String header : headers) {
            builder.append(header).append('\n');
        }

        OasRequestBody requestBody = operation.getRequestBody();
        if (requestBody != null && !requestBody.getContent().isEmpty()) {
            Map.Entry<String, OasSchema> content = requestBody.getContent().entrySet().iterator().next();
            builder.append("Content-Type: ").append(content.getKey()).append('\n');
            if (content.getKey().contains("json")) {
                builder.append('\n');
                builder.append(HttpRequestBodySampler.sample(content.getValue())).append('\n');
            }
        }
        else if (requestBody != null || httpMethod == OasHttpMethod.POST || httpMethod == OasHttpMethod.PUT || httpMethod == OasHttpMethod.PATCH) {
            builder.append("Content-Type: ").append(APPLICATION_JSON).append('\n');
            builder.append('\n');
            builder.append("{}\n");
        }
    }

    private static String findAccept(OasOperation operation) {
        for (OasResponse response : operation.getResponses()) {
            for (String contentType : response.getContent().keySet()) {
                return contentType;
            }
        }
        return APPLICATION_JSON;
    }

    private static String toTemplate(String path) {
        Matcher matcher = PATH_VARIABLE.matcher(path);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement("{{" + matcher.group(1) + "}}"));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
