package com.kerrybarnes.utilities.persistence;

import com.kerrybarnes.utilities.UserPropertyPersistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UserPropertiesInitialLoader implements UserPropertyPersistence {

    private static final String INITIAL_LOAD = "{\n" +
            "  \"string.property\" : {\n" +
            "    \"key\" : \"string.property\",\n" +
            "    \"type\" : \"String\",\n" +
            "    \"value\" : \"initial value\"\n" +
            "  },\n" +
            "  \"double.property\" : {\n" +
            "    \"key\" : \"double.property\",\n" +
            "    \"type\" : \"Double\",\n" +
            "    \"value\" : \"100.0\"\n" +
            "  },\n" +
            "  \"int.property\" : {\n" +
            "    \"key\" : \"int.property\",\n" +
            "    \"type\" : \"Integer\",\n" +
            "    \"value\" : \"10\"\n" +
            "  }\n" +
            "}";

    protected ByteArrayOutputStream baos;

    @Override
    public InputStream getInputStream() throws IOException {
        final byte[] data;
        if (baos == null) {
            data = INITIAL_LOAD.getBytes();
        } else {
            data = baos.toByteArray();
        }

        return new ByteArrayInputStream(data);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        baos = new ByteArrayOutputStream();
        return baos;
    }

    @Override
    public boolean exists() {
        return true;
    }

    public byte[] getBytes() {
        if (baos == null) {
            return INITIAL_LOAD.getBytes();
        }
        return baos.toByteArray();
    }
}
