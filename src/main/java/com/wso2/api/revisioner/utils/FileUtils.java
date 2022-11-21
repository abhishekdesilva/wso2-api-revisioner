package com.wso2.api.revisioner.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.FileHandler;

/**
 * Utility class to handle file operations.
 *
 * @since 1.0.0
 */
public class FileUtils {

    public static final String CONF_FILE_NAME = "./resources/integration.properties";
    public static final String LOG_FILE_NAME = "./resources/revisioner.log";
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

    public static FileWriter getNewFileWriter(){
        try {
            File myObj = new File(LOG_FILE_NAME);
            if (myObj.createNewFile()) {
                System.out.println("Log file created in the resources directory : " + myObj.getName());
                return new FileWriter(LOG_FILE_NAME, true);
            } else {
                System.out.println("Log file already exist in the resources directory. Deleting the file.");
                myObj.delete();
                myObj.createNewFile();
                return new FileWriter(LOG_FILE_NAME, true);
            }

        } catch (IOException e) {
            log.error("Error while creating the revisioner.log file", e);
        }
        return null;
    }
}
