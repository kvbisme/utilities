package com.kerrybarnes.uitilities.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.beans.property.Property;

import java.io.IOException;

public class UserPropertySerializer extends JsonSerializer<Property> {

    @Override
    public void serialize(Property value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("key", value.getName());
        gen.writeStringField("type", value.getValue().getClass().getSimpleName());
        gen.writeStringField("value", value.getValue().toString());
        gen.writeEndObject();
    }
}
