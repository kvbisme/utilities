package com.kerrybarnes.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface UserPropertyPersistence {
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
    boolean exists();
}
