package com.kerrybarnes.uitilities.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;

public class UserPropertyDeSerializer extends JsonDeserializer<Property>  {
    @Override
    public Property deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        final String key = node.get("key").asText();
        final String type = node.get("type").asText();
        final String value = node.get("value").asText();

        final Property<Object> result;
        switch(type) {
            case "Double":
                result = new SimpleObjectProperty<>(null, key, Double.parseDouble(value));
                break;
            case "Integer":
                result = new SimpleObjectProperty<>(null, key, Integer.parseInt(value));
                break;
            case "String":
                result = new SimpleObjectProperty<>(null, key, value);
                break;
            default:
                throw new RuntimeException("Found Unexpected Data Type: " + type);

        }

        return result;
    }
}
