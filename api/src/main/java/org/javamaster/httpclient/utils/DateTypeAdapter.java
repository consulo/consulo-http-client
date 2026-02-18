package org.javamaster.httpclient.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.util.Date;

/**
 * @author yudong
 */
public class DateTypeAdapter extends TypeAdapter<Date> {
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter writer, Date value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            String dateFormatAsString = DATE_FORMAT.format(value);
            writer.value(dateFormatAsString);
        }
    }

    @Override
    public Date read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        } else {
            String date = reader.nextString();
            try {
                return DATE_FORMAT.parse(date);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
