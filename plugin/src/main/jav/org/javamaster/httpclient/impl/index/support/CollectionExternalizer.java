package org.javamaster.httpclient.impl.index.support;

import consulo.index.io.data.DataExternalizer;
import consulo.index.io.DataInputOutputUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class CollectionExternalizer<T, C extends Map<T, T>> implements DataExternalizer<C> {
    private final DataExternalizer<T> elementExternalizer;
    private final Supplier<C> collectionFactory;

    public CollectionExternalizer(
            @NotNull DataExternalizer<T> elementExternalizer,
            @NotNull Supplier<C> collectionFactory
    ) {
        this.elementExternalizer = elementExternalizer;
        this.collectionFactory = collectionFactory;
    }

    @Override
    public void save(@NotNull DataOutput out, @NotNull C value) throws IOException {
        DataInputOutputUtil.writeINT(out, value.size());

        for (Map.Entry<T, T> entry : value.entrySet()) {
            elementExternalizer.save(out, entry.getKey());
            elementExternalizer.save(out, entry.getValue());
        }
    }

    @Override
    public @NotNull C read(@NotNull DataInput in) throws IOException {
        int size = DataInputOutputUtil.readINT(in);

        C value = collectionFactory.get();

        for (int i = 0; i < size; i++) {
            T k = elementExternalizer.read(in);
            T v = elementExternalizer.read(in);
            value.put(k, v);
        }

        return value;
    }
}
