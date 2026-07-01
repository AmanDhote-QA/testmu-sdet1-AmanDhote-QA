package framework.config;


import framework.driver.BrowserType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_PATH = "src/main/resources/config/config.properties";

    private ConfigReader() {
    }

    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config.properties from path: " + CONFIG_PATH, e);
            }
        }
    }

    public static String get(String key) {
        loadProperties();
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing config key: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static BrowserType getBrowser() {
        String browserFromSystem = System.getProperty("browser");
        if (browserFromSystem != null && !browserFromSystem.isBlank()) {
            return BrowserType.from(browserFromSystem);
        }
        return BrowserType.from(get("browser"));
    }

    public static String getOpenAiApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENAI_API_KEY environment variable not set");
        }
        return apiKey;
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}