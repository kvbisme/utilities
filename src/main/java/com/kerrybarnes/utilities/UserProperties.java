package com.kerrybarnes.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kerrybarnes.utilities.json.UserPropertyDeSerializer;
import com.kerrybarnes.utilities.json.UserPropertySerializer;
import com.kerrybarnes.utilities.persistence.UserPropertiesFileBasedPersistence;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class UserProperties implements ChangeListener<Object> {
    private static Logger log = LogManager.getLogger(UserProperties.class);

    public final static String PERSISTENCE_KEY = "user.props.persistence";
    public final static String DEFAULT_PERSISTENCE_CLASS = UserPropertiesFileBasedPersistence.class.getName();

    private final static UserProperties instance = new UserProperties(false);
    private final Map<String, Property<Object>> properties;

    private final UserPropertyPersistence persistence;

    private final JsonSerializer<Property> serializer = new UserPropertySerializer();
    private final JsonDeserializer<Property> deSerializer = new UserPropertyDeSerializer();
    private final ObjectMapper mapper;
    private final TypeReference<TreeMap<String, Property>> typeRef = new TypeReference<TreeMap<String, Property>>() {};

    protected UserProperties(final boolean isSyncronized)
    {
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();

        module.addDeserializer(Property.class, deSerializer);
        module.addSerializer(Property.class, serializer);
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        final String clazzName = System.getProperty(PERSISTENCE_KEY, DEFAULT_PERSISTENCE_CLASS);
        try {
            final Class<?> persistenceClass = Class.forName(clazzName);
            persistence = (UserPropertyPersistence)persistenceClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            final String msg = String.format("Unable to create persistence layer, reason: %s", e.toString());
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        final Map<String,Property<Object>> propertyMap = load();
        if (isSyncronized) {
            properties = Collections.synchronizedMap(propertyMap);
        } else {
            properties = propertyMap;
        }
    }

    protected Map<String, Property<Object>> load() {
        final Map<String, Property<Object>> properties;
        if (persistence.exists()) {
            try {
                properties = mapper.readValue(persistence.getInputStream(), typeRef);
                properties.values().stream().forEach(p -> {
                    p.addListener(this);
                } );
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
            mapper.writeValue(persistence.getOutputStream(), properties);
        } catch (IOException e) {
            log.error("Error Updating User Properties File", e);
        }
    }

    public Class<?> getType(final String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        final Property<Object> property = properties.get(key);
        return property.getValue().getClass();
    }

    public int getIntProperty(final String key) {
        return getIntProperty(key, 0);
    }

    protected UserPropertyPersistence getPersistence() {
        return persistence;
    }

    protected void clear() {
        this.properties.clear();
        update();
    }

    public int getIntProperty(final String key, final int defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(new Integer(defaultValue), key);
            property.addListener(this);
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
            property.addListener(this);
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
            property.addListener(this);
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
            property.addListener(this);
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
            property.addListener(this);
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
            property.addListener(this);
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

    @Override
    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
        update();
    }

    public static UserProperties getInstance() {
        return instance;
    }
}
