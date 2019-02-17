package com.kerrybarnes.utilities.persistence;

import com.kerrybarnes.utilities.UserPropertyPersistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UserPropertiesByteArrayPersistence implements UserPropertyPersistence {

    private ByteArrayOutputStream baos;

    public UserPropertiesByteArrayPersistence() {

    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public OutputStream getOutputStream() {
        baos = new ByteArrayOutputStream();
        return baos;
    }

    @Override
    public boolean exists() {
        return false;
    }

    public byte[] getBytes() {
        if (baos == null) {
            return null;
        }

        return baos.toByteArray();
    }

    public void reset() {
        baos = null;
    }
}
