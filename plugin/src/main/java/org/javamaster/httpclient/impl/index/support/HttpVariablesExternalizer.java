package org.javamaster.httpclient.impl.index.support;

import consulo.index.io.data.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class HttpVariablesExternalizer extends CollectionExternalizer<String, Map<String, String>> {
    public HttpVariablesExternalizer(
            @NotNull DataExternalizer<String> elementExternalizer,
            @NotNull Supplier<Map<String, String>> collectionFactory
    ) {
        super(elementExternalizer, collectionFactory);
    }
}
