package com.kerrybarnes.uitilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kerrybarnes.uitilities.json.UserPropertyDeSerializer;
import com.kerrybarnes.uitilities.json.UserPropertySerializer;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserProperties {
    private static Logger log = LogManager.getLogger(UserProperties.class);
    private final static UserProperties instance = new UserProperties(false);
    private final Path propertiesFilePath;
    private final Map<String, Property<Object>> properties;

    private final JsonSerializer<Property> serializer = new UserPropertySerializer();
    private final JsonDeserializer<Property> deSerializer = new UserPropertyDeSerializer();
    private final ObjectMapper mapper;
    private final TypeReference<HashMap<String, Property>> typeRef = new TypeReference<HashMap<String, Property>>() {};

    protected UserProperties(final boolean isSyncronized) {
        final String userHome = System.getProperty("user.home");
        final String userName = System.getProperty("user.name");
        final String propertiesFileName = String.format(".%s.local.properties", userName);
        propertiesFilePath = Paths.get(userHome, propertiesFileName);

        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();

        module.addDeserializer(Property.class, deSerializer);
        module.addSerializer(Property.class, serializer);
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);


        final Map<String,Property<Object>> propertyMap = load();
        if (isSyncronized) {
            properties = Collections.synchronizedMap(propertyMap);
        } else {
            properties = propertyMap;
        }
    }

    protected Map<String, Property<Object>> load() {
        final File propertiesFile = propertiesFilePath.toFile();
        final Map<String, Property<Object>> properties;
        if (propertiesFile.exists()) {
            try {
                properties = mapper.readValue(propertiesFilePath.toFile(), typeRef);
            } catch (IOException e) {
                final String msg = String.format("Error Loading User Properties File, reason: %s", e.toString());
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        } else {
            properties = new HashMap<>();
        }

        return properties;
    }

    protected void update() {
        try {
            mapper.writeValue(propertiesFilePath.toFile(), properties);
        } catch (IOException e) {
            log.error("Error Updating User Properties File", e);
        }
    }

    public int getIntProperty(final String key) {
        return getIntProperty(key, 0);
    }

    public int getIntProperty(final String key, final int defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(new Integer(defaultValue), key);
            properties.put(key, property);
        }

        return (Integer) property.getValue();
    }

    public int setIntProperty(final String key, final int newValue) {
        final int oldValue;
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
            oldValue = (Integer) property.getValue();
            property.setValue(newValue);
        } else {
            final Integer intValue = new Integer(newValue);
            property = new SimpleObjectProperty<>(null, key, intValue);
            properties.put(key, property);
            oldValue = 0;
        }

        update();
        return oldValue;
    }

    public double getDoubleProperty(final String key) {
        return getDoubleProperty(key, 0.0);
    }

    public double getDoubleProperty(final String key, final double defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(new Double(defaultValue), key);
            properties.put(key, property);
        }

        return (Double) property.getValue();
    }

    public double setDoubleProperty(final String key, final double newValue) {
        final double oldValue;
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
            oldValue = (Double) property.getValue();
            property.setValue(newValue);
        } else {
            final Double dblValue = new Double(newValue);
            property = new SimpleObjectProperty<>(null, key, dblValue);
            properties.put(key, property);
            oldValue = 0;
        }

        update();
        return oldValue;
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public String getProperty(String key, String defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(defaultValue, key);
            properties.put(key, property);
        }

        return (String)property.getValue();
    }

    public synchronized String setProperty(final String key, final String value) {
        final String oldValue;
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
            oldValue = (String) property.getValue();
            property.setValue(value);
        } else {
            property = new SimpleObjectProperty<>(null, key, value);
            properties.put(key, property);
            oldValue = null;
        }

        update();
        return oldValue;
    }

    public Set<String> propertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public <T> Property<T> property(final String key, Class<T> clazz) {
        if (!properties.containsKey(key)) {
            return null;
        }
        return (Property<T>) properties.get(key);
    }

    public static UserProperties getInstance() {
        return instance;
    }
}
