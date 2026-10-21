package consulo.httpClient.impl.internal.endpoint;

import consulo.endpoint.oas.OasProperty;
import consulo.endpoint.oas.OasSchema;
import consulo.endpoint.oas.OasSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class HttpRequestBodySampler {
    private static final int MAX_DEPTH = 4;
    private static final String INDENT = "  ";

    private HttpRequestBodySampler() {
    }

    public static String sample(@Nullable OasSchema schema) {
        StringBuilder builder = new StringBuilder();
        append(builder, schema, 0);
        return builder.toString();
    }

    private static void append(StringBuilder builder, @Nullable OasSchema schema, int depth) {
        OasSchemaType type = schema == null ? null : schema.getType();
        if (schema == null || type == null) {
            builder.append("{}");
            return;
        }
        switch (type) {
            case OBJECT -> appendObject(builder, schema, depth);
            case ARRAY -> {
                if (depth >= MAX_DEPTH) {
                    builder.append("[]");
                    return;
                }
                builder.append('[');
                append(builder, schema.getItems(), depth + 1);
                builder.append(']');
            }
            case STRING -> {
                List<String> values = schema.getEnum();
                builder.append('"').append(values == null || values.isEmpty() ? "" : escape(values.get(0))).append('"');
            }
            case INTEGER, NUMBER -> builder.append('0');
            case BOOLEAN -> builder.append("false");
        }
    }

    private static void appendObject(StringBuilder builder, OasSchema schema, int depth) {
        List<OasProperty> properties = schema.getProperties();
        if (properties == null || properties.isEmpty() || depth >= MAX_DEPTH) {
            builder.append("{}");
            return;
        }
        String indent = INDENT.repeat(depth + 1);
        builder.append("{\n");
        for (int i = 0; i < properties.size(); i++) {
            OasProperty property = properties.get(i);
            builder.append(indent).append('"').append(escape(property.getName())).append("\": ");
            append(builder, property.getSchema(), depth + 1);
            if (i < properties.size() - 1) {
                builder.append(',');
            }
            builder.append('\n');
        }
        builder.append(INDENT.repeat(depth)).append('}');
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
