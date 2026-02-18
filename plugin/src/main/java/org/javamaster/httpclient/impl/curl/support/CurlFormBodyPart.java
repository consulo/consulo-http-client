package org.javamaster.httpclient.impl.curl.support;

import consulo.util.lang.StringUtil;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CurlFormBodyPart {
    protected String myFieldName;
    private List<CurlRequest.KeyValuePair> myHeaders = new ArrayList<>();

    public CurlFormBodyPart(String myFieldName) {
        this.myFieldName = myFieldName;
    }

    public CurlFormBodyPart addHeader(String name, String value) {
        myHeaders.add(new CurlRequest.KeyValuePair(name, value));
        return this;
    }

    protected FormBodyPartBuilder fillHeaders(FormBodyPartBuilder builder) {
        for (CurlRequest.KeyValuePair header : myHeaders) {
            builder.addField(header.key, header.value);
        }
        return builder;
    }

    public abstract FormBodyPart toBodyPart();

    public String toPsiRepresentation() {
        return myHeaders.stream()
                .map(it -> it.key + ": " + it.value)
                .collect(Collectors.joining("\n"));
    }

    private static class CurlStringBodyPart extends CurlFormBodyPart {
        private final String myContent;
        private final ContentType contentType;

        public CurlStringBodyPart(String fieldName, String myContent, ContentType contentType) {
            super(fieldName);
            this.myContent = myContent;
            this.contentType = contentType;
        }

        @Override
        public FormBodyPart toBodyPart() {
            FormBodyPartBuilder builder = FormBodyPartBuilder.create(myFieldName, new StringBody(myContent, contentType));
            return fillHeaders(builder).build();
        }

        @Override
        public String toPsiRepresentation() {
            return super.toPsiRepresentation() + "\n\n" + StringUtil.convertLineSeparators(myContent);
        }
    }

    private static class CurlFileBodyPart extends CurlFormBodyPart {
        private final String myFileName;
        private final File myFile;
        private final ContentType contentType;

        public CurlFileBodyPart(String name, String myFileName, File myFile, ContentType contentType) {
            super(name);
            this.myFileName = myFileName;
            this.myFile = myFile;
            this.contentType = contentType;
        }

        @Override
        public FormBodyPart toBodyPart() {
            FormBodyPartBuilder builder = FormBodyPartBuilder.create(myFieldName, new FileBody(myFile, contentType, myFileName));
            return fillHeaders(builder).build();
        }

        @Override
        public String toPsiRepresentation() {
            return super.toPsiRepresentation() + "\n\n< " + myFile.getAbsolutePath();
        }
    }

    public static CurlFormBodyPart create(String fieldName, String fileName, File file, ContentType contentType) {
        return new CurlFileBodyPart(fieldName, fileName, file, contentType);
    }

    public static CurlFormBodyPart create(String fieldName, String content, ContentType contentType) {
        return new CurlStringBodyPart(fieldName, content, contentType);
    }
}
