package org.javamaster.httpclient.impl.index.support;

import consulo.index.io.data.DataExternalizer;
import consulo.index.io.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringDataExternalizer implements DataExternalizer<String> {
    @Override
    public void save(@NotNull DataOutput out, @NotNull String value) throws IOException {
        IOUtil.writeUTF(out, value);
    }

    @Override
    public @NotNull String read(@NotNull DataInput in) throws IOException {
        return IOUtil.readUTF(in);
    }
}
