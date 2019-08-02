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

/**
 * Maintains a {@link Map} of properties similar to the original {@link java.util.Properties} class.  The differences
 * in this implementation is that the values are automatically persisted when modified, setters and accessors are available
 * for String, Integer, and Double values, and the values are stored as {@link Property} objects so they can be bound
 * to JavaFX properties.
 *
 * This is an initial release and there are already things I am not find of.  In my next iteration I believe this
 * will be instantiated with a Builder to customize the persistence file name and location.  Plus I htink I want to take
 * the persistence into a background thread, currently this is all synchronous. I should probably go ahead and add Long
 * and Float types as well.
 *
 * In the meantime if you want to customize the persistence you will need to create a class implementing the
 * {@link UserPropertyPersistence} interface and then specify it as a Systems property (see why I am not fond of this
 * yet?) <code>-Duser.props.persistence=${Your ClassName}</code>
 */
public class UserProperties {
    private static Logger log = LogManager.getLogger(UserProperties.class);

    public final static String PERSISTENCE_KEY = "user.props.persistence";
    public final static String DEFAULT_PERSISTENCE_CLASS = UserPropertiesFileBasedPersistence.class.getName();

    private final static UserProperties instance = new UserProperties(false);
    private final Map<String, Property> properties;

    private final UserPropertyPersistence persistence;

    private final JsonSerializer<Property> serializer = new UserPropertySerializer();
    private final JsonDeserializer<Property> deSerializer = new UserPropertyDeSerializer();
    private final ObjectMapper mapper;
    private final TypeReference<TreeMap<String, Property>> typeRef = new TypeReference<TreeMap<String, Property>>() {};

    private final ChangeListener<Object> propertyChangeListener = new ChangeListener<Object>() {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            update();
        }
    };

    /**
     * Protected constructor for use in unit testing.  Normally you would obtain an instance
     * using the {@link #getInstance()}
     *
     * @param isSyncronized will wrap the underlying {@link TreeMap} so it is synchronized
     *                      if set to true. (future implementaion, right now this value
     *                      is always false)
     */
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

        final Map<String,Property> propertyMap = load();
        if (isSyncronized) {
            properties = Collections.synchronizedMap(propertyMap);
        } else {
            properties = propertyMap;
        }
    }

    /**
     * Loads the persistent copy from disk if present
     *
     * @return a new {@link Map} if there is no persisted copy.  If the persisted data
     *         was found a {@link Map} containing the properties
     */
    protected Map<String, Property> load() {
        final Map<String, Property> properties;
        if (persistence.exists()) {
            try {
                properties = mapper.readValue(persistence.getInputStream(), typeRef);
                properties.values().stream().forEach(p -> {
                    p.addListener(propertyChangeListener);
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

    /**
     * Updates the persisted copy of the properties.
     */
    protected void update() {
        try {
            mapper.writeValue(persistence.getOutputStream(), properties);
        } catch (IOException e) {
            log.error("Error Updating User Properties File", e);
        }
    }

    /**
     * Added for unit testing to retrieve the persistence implementation
     *
     * @return the implementation of {@link UserPropertyPersistence} used
     *         to retrieve and persist the properties
     */
    protected UserPropertyPersistence getPersistence() {
        return persistence;
    }

    /**
     * Added for Unit testing, might consider making this public in the future
     * but I feel it is pretty dangerous.  If you accidentally call it your
     * properties will be lost forever.
     */
    protected void clear() {
        this.properties.clear();
        update();
    }

    /**
     * Retrieve the {@link Integer int} value of the supplied property
     * name, or key.  Will return 0 and create a property with a value of
     * 0 if the property does not exist.
     *
     * @param key the property name, or key
     * @return an {@link Integer int} value, 0 if the property does not exist
     */
    public int getIntProperty(final String key) {
        return getIntProperty(key, 0);
    }

    /**
     * Retrieve the {@link Integer int} value of the supplied property name, or key.
     * If the property does not exist a new property supplied default value will be
     * created a the default value will be returned
     *
     * @param key the property name, or key
     * @param defaultValue the value to use of the requested property does not exist
     * @return the {@link Integer int} value if the property or the supplied default
     *         value if the property does not exist
     */
    public int getIntProperty(final String key, final int defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            final Integer intValue = Integer.valueOf(defaultValue);
            property = new SimpleObjectProperty<>(null, key, intValue);
            property.addListener(propertyChangeListener);
            properties.put(key, property);
        }

        return (Integer) property.getValue();
    }

    /**
     * Creates or Updates an Integer property using the supplied property name, or key,
     * and integer value.  The new or updated value is also persisted to the backend
     * store as well
     *
     * @param key the property name, or key
     * @param newValue the new value the property will contain
     * @return the previous value of this property or 0 if the property
     *         is new
     */
    public int setIntProperty(final String key, final int newValue) {
        final int oldValue;
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
            oldValue = (Integer) property.getValue();
            property.setValue(newValue);
        } else {
            final Integer intValue = Integer.valueOf(newValue);
            property = new SimpleObjectProperty<>(null, key, intValue);
            property.addListener(propertyChangeListener);
            properties.put(key, property);
            oldValue = 0;
        }

        update();
        return oldValue;
    }

    /**
     * Retrieve the {@link Double double} value of the supplied property
     * name, or key.  Will return 0.0 and create a property with a value of
     * 0.0 if the property does not exist.
     *
     * @param key the property name, or key
     * @return a {@link Double double} value, 0.0 if the property does not exist
     */
    public double getDoubleProperty(final String key) {
        return getDoubleProperty(key, 0.0);
    }

    /**
     * Retrieve the {@link Double double} value of the supplied property name, or key.
     * If the property does not exist a new property supplied default value will be
     * created a the default value will be returned
     *
     * @param key the property name, or key
     * @param defaultValue the value to use if the requested property does not exist
     * @return the {@link Double double} value of the property or the supplied default
     *         value if the property does not exist
     */
    public double getDoubleProperty(final String key, final double defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(null, key, Double.valueOf(defaultValue));
            property.addListener(propertyChangeListener);
            properties.put(key, property);
        }

        return (Double) property.getValue();
    }

    /**
     * Creates or Updates an Double property using the supplied property name, or key,
     * and double value.  The new or updated value is also persisted to the backend
     * store as well
     *
     * @param key the property name, or key
     * @param newValue the new value the property will contain
     * @return the previous value of this property or 0.0 if the property
     *         is new
     */
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
            property.addListener(propertyChangeListener);
            properties.put(key, property);
            oldValue = 0;
        }

        update();
        return oldValue;
    }

    /**
     * Retrieve the {@link String} value of the supplied property
     * name, or key.  Will return an empty {@link String} and create a property
     * with a value of the empty {@link String} if the property does not exist.
     *
     * @param key the property name, or key
     * @return a {@link String} value, or an empty {@link String} if the property
     *         does not exist
     */
    public String getProperty(String key) {
        return getProperty(key, "");
    }

    /**
     * Retrieve the {@link String} value of the supplied property name, or key.
     * If the property does not exist a new property with the supplied default
     * value will be created a the default value will be returned
     *
     * @param key the property name, or key
     * @param defaultValue the value to use if the requested property does not exist
     * @return the {@link String} value of the property or the supplied default
     *         value if the property does not exist
     */
    public String getProperty(String key, String defaultValue) {
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
        } else {
            property = new SimpleObjectProperty<>(null, key, defaultValue);
            property.addListener(propertyChangeListener);
            properties.put(key, property);
        }

        return (String)property.getValue();
    }

    /**
     * Creates or Updates a String property using the supplied property name, or key,
     * and double value.  The new or updated value is also persisted to the backend
     * store as well
     *
     * @param key the property name, or key
     * @param value the new value the property will contain
     * @return the previous value of this property or an empty {@link String} if the
     *         property is new
     */
    public String setProperty(final String key, final String value) {
        final String oldValue;
        final Property<Object> property;
        if (properties.containsKey(key)) {
            property = properties.get(key);
            oldValue = (String) property.getValue();
            property.setValue(value);
        } else {
            property = new SimpleObjectProperty<>(null, key, value);
            property.addListener(propertyChangeListener);
            properties.put(key, property);
            oldValue = null;
        }

        update();
        return oldValue;
    }

    /**
     * A {@link Set} containing the property name, or key, values
     *
     * @return {@link Set Set&lt;String&gt;} of property names
     */
    public Set<String> propertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * Will return the {@link Class} of the proeprty defined by the
     * supplied Key
     *
     * @param key the property name, or key, indicating the property
     * @return returns the {@link Class} of the requested property or
     *         <code>null</code> if it is not defined.
     */
    public Class<?> getType(final String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        final Property<Object> property = properties.get(key);
        return property.getValue().getClass();
    }

    /**
     * Will return the {@link Property} wrapper of the underlying property
     *
     * @param key the property name, or key value
     * @param clazz the {@link Class} of the property for use in casting
     * @param <T> the {@link Class} of the underlying property
     * @return the {@link Property} for the supplied property name or
     *         <code>null</code> if the property does not exist
     * @throws ClassCastException if you request the wrong {@link Class}
     *         of the property, for example you request a
     *         {@link Integer Integer.class} for a {@link String String.class}
     *         property.
     */
    public <T> Property<T> property(final String key, Class<T> clazz) {
        if (!properties.containsKey(key)) {
            return null;
        }
        return (Property<T>) properties.get(key);
    }

    /**
     * Return the instance of the User Properties
     *
     * @return the singleton instance
     */
    public static UserProperties getInstance() {
        return instance;
    }
}
