package com.kerrybarnes.utilities;

import com.kerrybarnes.utilities.persistence.UserPropertiesInitialLoader;
import javafx.beans.property.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserPropertyLoaderTestCase {
    private static final String AFTER_STRING_UPDATE = "{\n" +
            "  \"double.property\" : {\n" +
            "    \"key\" : \"double.property\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"100.0\"\n" +
            "  },\n" +
            "  \"int.property\" : {\n" +
            "    \"key\" : \"int.property\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"10\"\n" +
            "  },\n" +
            "  \"string.property\" : {\n" +
            "    \"key\" : \"string.property\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated using listener\"\n" +
            "  }\n" +
            "}";
    private static final String AFTER_INT_UPDATE = "{\n" +
            "  \"double.property\" : {\n" +
            "    \"key\" : \"double.property\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"100.0\"\n" +
            "  },\n" +
            "  \"int.property\" : {\n" +
            "    \"key\" : \"int.property\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"12\"\n" +
            "  },\n" +
            "  \"string.property\" : {\n" +
            "    \"key\" : \"string.property\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated using listener\"\n" +
            "  }\n" +
            "}";
    private static final String AFTER_DOUBLE_UPDATE = "{\n" +
            "  \"double.property\" : {\n" +
            "    \"key\" : \"double.property\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"400.0\"\n" +
            "  },\n" +
            "  \"int.property\" : {\n" +
            "    \"key\" : \"int.property\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"12\"\n" +
            "  },\n" +
            "  \"string.property\" : {\n" +
            "    \"key\" : \"string.property\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated using listener\"\n" +
            "  }\n" +
            "}";

    private UserProperties props;
    private UserPropertiesInitialLoader loader;

    @Before
    public void intialize() {
        System.setProperty(UserProperties.PERSISTENCE_KEY, UserPropertiesInitialLoader.class.getName());
        props = new UserProperties(false);
        loader = (UserPropertiesInitialLoader)props.getPersistence();
    }
    @After
    public void reset() {

    }

    @Test
    public void testLoadedStringPropertyHasListeners() throws Exception {

        final Property<String> stringProperty = props.property("string.property", String.class);
        assertNotNull(stringProperty);
        stringProperty.setValue("updated using listener");
        assertEquals(new String(loader.getBytes()), AFTER_STRING_UPDATE);

        final Property<Integer> intProperty = props.property("int.property", Integer.class);
        assertNotNull(intProperty);
        intProperty.setValue(12);
        assertEquals(new String(loader.getBytes()), AFTER_INT_UPDATE);

        final Property<Double> doubleProperty = props.property("double.property", Double.class);
        assertNotNull(doubleProperty);
        doubleProperty.setValue(400.0);
        assertEquals(new String(loader.getBytes()), AFTER_DOUBLE_UPDATE);
    }
}
