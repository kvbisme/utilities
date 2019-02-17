package com.kerrybarnes.utilities.persistence;

import com.kerrybarnes.utilities.UserPropertyPersistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserPropertiesFileBasedPersistence implements UserPropertyPersistence {
    private final Path filePath;

    public UserPropertiesFileBasedPersistence() {
        final String userHome = System.getProperty("user.home");
        final String userName = System.getProperty("user.name");
        final String propertiesFileName = String.format(".%s.local.properties", userName);
        filePath = Paths.get(userHome, propertiesFileName);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(filePath);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(filePath);
    }

    @Override
    public boolean exists() {
        return filePath.toFile().exists();
    }
}
