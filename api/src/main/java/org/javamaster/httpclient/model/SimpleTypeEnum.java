package org.javamaster.httpclient.model;

public enum SimpleTypeEnum {
    JSON("json", false),
    HTML("html", false),
    XML("xml", false),
    TXT("txt", false),
    TEXT("text", false),

    STREAM("stream", true),
    IMAGE("image", true),
    PDF("pdf", true),
    EXCEL("excel", true),
    ZIP("zip", true);

    private final String type;
    private final boolean binary;

    SimpleTypeEnum(String type, boolean binary) {
        this.type = type;
        this.binary = binary;
    }

    public String getType() {
        return type;
    }

    public boolean isBinary() {
        return binary;
    }

    public static boolean isTextContentType(String contentType) {
        SimpleTypeEnum simpleTypeEnum = convertContentType(contentType);
        return !simpleTypeEnum.binary;
    }

    public static String getSuffix(SimpleTypeEnum simpleTypeEnum, String contentType) {
        switch (simpleTypeEnum) {
            case IMAGE:
                String[] parts = contentType.split("/");
                return parts[parts.length - 1];
            case TEXT:
                return "txt";
            case EXCEL:
                return "xls";
            case STREAM:
                return "bin";
            default:
                return simpleTypeEnum.type;
        }
    }

    public static SimpleTypeEnum convertContentType(String contentType) {
        if (contentType.contains(JSON.type)) {
            return JSON;
        }

        if (contentType.contains(HTML.type)) {
            return HTML;
        }

        if (contentType.contains(XML.type)) {
            return XML;
        }

        if (contentType.contains(TEXT.type)) {
            return TEXT;
        }

        if (contentType.contains(TXT.type)) {
            return TXT;
        }

        if (contentType.contains(IMAGE.type)) {
            return IMAGE;
        }

        if (contentType.contains(PDF.type)) {
            return PDF;
        }

        if (contentType.contains(EXCEL.type)) {
            return EXCEL;
        }

        if (contentType.contains(ZIP.type)) {
            return ZIP;
        }

        return STREAM;
    }
}
