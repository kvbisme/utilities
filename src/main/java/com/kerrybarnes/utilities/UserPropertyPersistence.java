package com.kerrybarnes.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines the Inteface used by {@link UserProperties} to
 * get the {@link InputStream} and {@link OutputStream} for
 * loading and persisting the contents of the
 * {@link UserProperties}
 */
public interface UserPropertyPersistence {
    /**
     * Get an {@link InputStream} for loading the contents of
     * the {@link UserProperties}
     *
     * @return an {@link InputStream}
     * @throws IOException if an I/O error occurs
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get an {@link OutputStream} for storing the contents of
     * the {@link UserProperties}
     *
     * @return an {@link OutputStream}
     * @throws IOException if an I/O error occurs
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Determines if the persistence target to load/store the
     * properties from the {@link UserProperties} currently exists
     *
     * @return <code>true</code> if the persistence target exists,
     *         otherwise <code>false</code>
     */
    boolean exists();
}
