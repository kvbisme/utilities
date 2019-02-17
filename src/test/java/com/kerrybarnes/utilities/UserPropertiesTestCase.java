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
    private static String STRING_BINDING_UPDATE = "{\n" +
            "  \"string.example\" : {\n" +
            "    \"key\" : \"string.example\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"second\"\n" +
            "  }\n" +
            "}";
    private static String INTEGER_BINDING_UPDATE= "{\n" +
            "  \"int.example\" : {\n" +
            "    \"key\" : \"int.example\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"200\"\n" +
            "  }\n" +
            "}";
    private static String DOUBLE_BINDING_UPDATE= "{\n" +
            "  \"dbl.example\" : {\n" +
            "    \"key\" : \"dbl.example\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"200.0\"\n" +
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
    public void testPropertyUpdates() throws Exception {
        final String propName = "string.example";

        props.setProperty(propName, "first");
        final Property<String> stringProperty = props.property(propName, String.class);
        assertNotNull(stringProperty);

        stringProperty.setValue("second");
        assertEquals("second", props.getProperty(propName));

        final ChangeListener<String> listener = new TestListener<>("second","third");
        stringProperty.addListener(listener);
        props.setProperty(propName, "third");
        assertTrue(((TestListener) listener).wasCalled());
    }

    @Test
    public void testUserPropertyUpdates() throws Exception {
        final String propName = "string.example";

        props.setProperty(propName, "first");
        final Property<String> stringProperty = props.property(propName, String.class);
        assertNotNull(stringProperty);

        assertEquals("first", props.getProperty(propName));
        stringProperty.setValue("second");
        assertEquals("second", props.getProperty(propName));
        assertEquals(new String(testPersistence.getBytes()), STRING_BINDING_UPDATE);
    }

    @Test
    public void testIntPropertyUpdates() throws Exception {
        final String propName = "int.example";

        props.setIntProperty(propName, 42);
        final Property<Integer> intProperty = props.property(propName, Integer.class);
        assertNotNull(intProperty);

        intProperty.setValue(404);
        assertEquals(404, props.getIntProperty(propName));

        final ChangeListener<Integer> listener = new TestListener<>(404,200);
        intProperty.addListener(listener);
        props.setIntProperty(propName, 200);
        assertTrue(((TestListener) listener).wasCalled());
    }

    @Test
    public void testIntUserPropertyUpdates() throws Exception {
        final String propName = "int.example";

        props.setIntProperty(propName, 100);
        final Property<Integer> intProperty = props.property(propName, Integer.class);
        assertNotNull(intProperty);

        assertEquals(100, props.getIntProperty(propName));
        intProperty.setValue(200);
        assertEquals(200, props.getIntProperty(propName));
        assertEquals(new String(testPersistence.getBytes()), INTEGER_BINDING_UPDATE);
    }

    @Test
    public void testDoublePropertyUpdates() throws Exception {
        final String propName = "double.example";

        props.setDoubleProperty(propName, 42.0);
        final Property<Double> dblProperty = props.property(propName, Double.class);
        assertNotNull(dblProperty);

        dblProperty.setValue(404.0);
        assertEquals(404.0, props.getDoubleProperty(propName),.01);

        final ChangeListener<Double> listener = new TestListener<>(404.0,200.0);
        dblProperty.addListener(listener);
        props.setDoubleProperty(propName, 200.0);
        assertTrue(((TestListener) listener).wasCalled());
    }

    @Test
    public void testDoubleUserPropertyUpdates() throws Exception {
        final String propName = "dbl.example";

        props.setDoubleProperty(propName, 100.0);
        final Property<Double> dblProperty = props.property(propName, Double.class);
        assertNotNull(dblProperty);

        assertEquals(100.0, props.getDoubleProperty(propName), .01);
        dblProperty.setValue(200.0);
        assertEquals(200.0, props.getDoubleProperty(propName), .01);
        assertEquals(new String(testPersistence.getBytes()), DOUBLE_BINDING_UPDATE);
    }

}

class TestListener<T> implements ChangeListener<T> {
    private boolean wasCalled = false;

    private final T before;
    private final T after;

    TestListener(final T before, final T after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        wasCalled = true;
        assertTrue(oldValue.equals(before));
        assertTrue(newValue.equals(after));
    }

    boolean wasCalled() {
        return wasCalled;
    }
}