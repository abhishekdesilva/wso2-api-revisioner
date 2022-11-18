package com.wso2.api.revisioner.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class to handle file operations.
 *
 * @since 1.0.0
 */
public class FileUtils {

    public static final String CONF_FILE_NAME = "./resources/integration.properties";
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);


    /**
     * Returns the property set specified in the configuration.
     *
     * @return configuration properties
     */
    public static Properties readConfiguration() {

        Path filePath = Paths.get(CONF_FILE_NAME);

        Properties properties = null;

        try {
            properties = getConfigProperties(filePath);
        } catch (IOException e) {
            log.error("Error while reading the integration.properties", e);
        }

        return properties;
    }

    /**
     * Returns the properties from the configuration file.
     *
     * @param filePath configuration file path to read properties
     * @return configuration properties
     */
    private static Properties getConfigProperties(Path filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            log.error("Error while reading the integration.properties", e);
            throw new IOException("Error while reading the integration.properties");
        }
    }
}
