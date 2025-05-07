package com.eaapp.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            // Load default properties from resources
            FileInputStream defaultConfig = new FileInputStream(
                System.getProperty("user.dir") + "/src/main/resources/config.properties");
            properties.load(defaultConfig);
            
            // Override with environment-specific properties if available
            String env = System.getProperty("env", "dev");
            FileInputStream envConfig = new FileInputStream(
                System.getProperty("user.dir") + "/src/main/resources/config-" + env + ".properties");
            properties.load(envConfig);
            logger.info("Loaded configuration for environment: {}", env);
        } catch (IOException e) {
            logger.error("Error loading configuration files", e);
        }
    }

    // Private constructor to prevent instantiation
    private ConfigReader() {}

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
