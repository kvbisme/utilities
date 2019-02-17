package com.kerrybarnes.utilities;

import com.kerrybarnes.utilities.persistence.UserPropertiesByteArrayPersistence;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class UserPropertiesTestCase {
    public final static String FIRST_VALUE_RESULT = "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"first\"\n" +
            "  }\n" +
            "}";
    public final static String FIRST_VALUE_UPDATED_RESULT = "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated\"\n" +
            "  }\n" +
            "}";
    private final static String ADD_INT_VALUE= "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated\"\n" +
            "  },\n" +
            "  \"test.first.int.value\" : {\n" +
            "    \"key\" : \"test.first.int.value\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"200\"\n" +
            "  }\n" +
            "}";
    private static final String UPDATED_INT_VALUE = "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated\"\n" +
            "  },\n" +
            "  \"test.first.int.value\" : {\n" +
            "    \"key\" : \"test.first.int.value\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"201\"\n" +
            "  }\n" +
            "}";
    private static final String ADD_DOUBLE_VALUE = "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated\"\n" +
            "  },\n" +
            "  \"test.first.dbl.value\" : {\n" +
            "    \"key\" : \"test.first.dbl.value\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"200.0\"\n" +
            "  },\n" +
            "  \"test.first.int.value\" : {\n" +
            "    \"key\" : \"test.first.int.value\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"201\"\n" +
            "  }\n" +
            "}";
    private static final String UPDATE_DOUBLE_VALUE = "{\n" +
            "  \"test.first.value\" : {\n" +
            "    \"key\" : \"test.first.value\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"updated\"\n" +
            "  },\n" +
            "  \"test.first.dbl.value\" : {\n" +
            "    \"key\" : \"test.first.dbl.value\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"201.0\"\n" +
            "  },\n" +
            "  \"test.first.int.value\" : {\n" +
            "    \"key\" : \"test.first.int.value\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"201\"\n" +
            "  }\n" +
            "}";

    private UserProperties props = null;
    private UserPropertiesByteArrayPersistence testPersistence = null;

    @Before
    public void setupTest() {
        System.setProperty(UserProperties.PERSISTENCE_KEY, UserPropertiesByteArrayPersistence.class.getName());
        props = UserProperties.getInstance();
        final UserPropertyPersistence persistence = props.getPersistence();
        assertNotNull(persistence);
        assertTrue(persistence instanceof UserPropertiesByteArrayPersistence);
        testPersistence = (UserPropertiesByteArrayPersistence)persistence;
    }

    @After
    public void reset() {
        props.clear();
        testPersistence.reset();
    }

    @Test
    public void checkPropertySerialization() throws Exception {
        props.setProperty("test.first.value", "first");
        assertEquals(new String(testPersistence.getBytes()), FIRST_VALUE_RESULT);
        props.setProperty("test.first.value", "updated");
        assertEquals(new String(testPersistence.getBytes()), FIRST_VALUE_UPDATED_RESULT);
        props.setIntProperty("test.first.int.value", 200);
        assertEquals(new String(testPersistence.getBytes()), ADD_INT_VALUE);
        props.setIntProperty("test.first.int.value", 201);
        assertEquals(new String(testPersistence.getBytes()), UPDATED_INT_VALUE);
        props.setDoubleProperty("test.first.dbl.value", 200.0);
        assertEquals(new String(testPersistence.getBytes()), ADD_DOUBLE_VALUE);
        props.setDoubleProperty("test.first.dbl.value", 201.0);
        assertEquals(new String(testPersistence.getBytes()), UPDATE_DOUBLE_VALUE);
    }

    @Test
    public void testIntPropertyUpdates() throws Exception {
        final String propName = "int.example";
        final UserProperties props = UserProperties.getInstance();
        final UserPropertyPersistence persistence = props.getPersistence();
        assertNotNull(persistence);
        assertTrue(persistence instanceof UserPropertiesByteArrayPersistence);
        final UserPropertiesByteArrayPersistence testPersistence = (UserPropertiesByteArrayPersistence)persistence;

        props.setIntProperty("int.example", 42);
        final Property<Integer> intProperty = props.property(propName, Integer.class);
        assertNotNull(intProperty);

        intProperty.setValue(404);
        assertEquals(404, props.getIntProperty(propName));

        final ChangeListener<Integer> listener = new IntListener(404,200);
        intProperty.addListener(listener);
        props.setIntProperty(propName, 200);
        assertTrue(((IntListener) listener).wasCalled());
    }
}

class IntListener implements ChangeListener<Integer> {
    private boolean wasCalled = false;

    private final int before;
    private final int after;

    IntListener(final int before, final int after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
        wasCalled = true;
        assertTrue(oldValue.equals(before));
        assertTrue(newValue.equals(after));
    }

    boolean wasCalled() {
        return wasCalled;
    }
}