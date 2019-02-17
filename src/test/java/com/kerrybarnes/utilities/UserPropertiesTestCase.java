package com.kerrybarnes.utilities;

import com.kerrybarnes.uitilities.UserProperties;
import org.junit.Test;

public class UserPropertiesTestCase {
    @Test
    public void checkPropertySerialization() {
        final UserProperties props = UserProperties.getInstance();
        props.setProperty("test.first.value", "first");
        props.setProperty("test.first.value", "updated");
        props.setIntProperty("test.first.int.value", 200);
        props.setIntProperty("test.first.int.value", 201);
        props.setDoubleProperty("test.first.dbl.value", 200.0);
        props.setDoubleProperty("test.first.dbl.value", 201.0);
    }
}
