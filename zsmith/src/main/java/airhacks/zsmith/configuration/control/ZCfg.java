package airhacks.zsmith.configuration.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Configuration loader that reads properties in order:
 * 1. ~/.[appName]/app.properties (global)
 * 2. ./app.properties (local, overwrites global)
 * 3. System properties (highest priority)
 * 
 * <pre>
 * // Initialize once at application startup
 * ZCfg.loadBaseConfig("myapp");
 * 
 * // Access configuration values
 * var port = ZCfg.integer("server.port", 8080);
 * var debug = ZCfg.bool("debug.enabled", false);
 * var dbUrl = ZCfg.string("db.url", "localhost:5432");
 * </pre>
 */
public class ZCfg {

    static final String PROPERTIES_FILE = "app.properties";
    static final String SYSTEM_PROMPT_FILE = "system.prompt";
    static Properties CACHE;
    static String APP_NAME;

    public static void loadBaseConfig(String appName) {
        APP_NAME = appName;
        CACHE = loadProperties(appName);
    }

    /**
     * Loads properties from global (~/.{appName}/app.properties), local (./app.properties),
     * and system properties, with each layer overwriting the previous.
     *
     * @param appName used to locate the global config directory
     * @return merged properties with system properties taking highest priority
     */
    static Properties loadProperties(String appName) {
        var properties = new Properties();

        // Load global properties from ~/.[appName]/app.properties
        var userHome = System.getProperty("user.home");
        var globalConfig = Path.of(userHome, "." + appName, PROPERTIES_FILE);
        if (Files.exists(globalConfig)) {
            loadFromFile(globalConfig, properties);
        }

        // Load local properties from ./app.properties (overwrites global)
        var localConfig = Path.of(PROPERTIES_FILE);
        if (Files.exists(localConfig)) {
            loadFromFile(localConfig, properties);
        }

        // System properties have highest priority
        properties.putAll(System.getProperties());

        return properties;
    }

    /**
     * Loads configuration for a named agent, overriding base properties with values
     * from ~/.{appName}/{agentName}/app.properties and ./{agentName}/app.properties.
     *
     * @param agentName the agent instance name used to locate its configuration
     * @throws IllegalStateException if {@link #loadBaseConfig(String)} has not been called first
     */
    public static void loadNamedAgentConfig(String agentName) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        var userHome = System.getProperty("user.home");
        var globalAgentConfig = Path.of(userHome, "." + APP_NAME, agentName, PROPERTIES_FILE);
        if (Files.exists(globalAgentConfig)) {
            loadFromFile(globalAgentConfig, CACHE);
        }
        var localAgentConfig = Path.of(agentName, PROPERTIES_FILE);
        if (Files.exists(localAgentConfig)) {
            loadFromFile(localAgentConfig, CACHE);
        }
    }

    /**
     * Loads a system prompt from system.prompt files in order:
     * 1. ~/.{appName}/{agentName}/system.prompt (global agent-specific)
     * 2. ./{agentName}/system.prompt (local agent-specific)
     * 3. ./system.prompt (highest priority)
     *
     * Each layer overwrites the previous.
     *
     * @param agentName the agent instance name
     * @return the system prompt content, or null if no file exists
     */
    public static String loadSystemPrompt(String agentName) {
        var userHome = System.getProperty("user.home");
        String prompt = null;
        var globalPrompt = Path.of(userHome, "." + APP_NAME, agentName, SYSTEM_PROMPT_FILE);
        if (Files.exists(globalPrompt)) {
            prompt = readTextFile(globalPrompt);
        }
        var localPrompt = Path.of(agentName, SYSTEM_PROMPT_FILE);
        if (Files.exists(localPrompt)) {
            prompt = readTextFile(localPrompt);
        }
        var basePrompt = Path.of(SYSTEM_PROMPT_FILE);
        if (Files.exists(basePrompt)) {
            prompt = readTextFile(basePrompt);
        }
        return prompt;
    }

    static String readTextFile(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file: " + file, e);
        }
    }

    static void loadFromFile(Path file, Properties properties) {
        try (var is = Files.newBufferedReader(file)) {
            properties.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load properties from: " + file, e);
        }
    }

    public static String string(String key) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        return CACHE.getProperty(key);
    }

    public static String string(String key, String defaultValue) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        return CACHE.getProperty(key, defaultValue);
    }

    public static String requiredString(String key) {
        var value = string(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: '" + key + "'");
        }
        return value;
    }

    public static int integer(String key, int defaultValue) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        var value = CACHE.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static boolean bool(String key, boolean defaultValue) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        var value = CACHE.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Retrieves comma-separated values as a list, trimming whitespace from each element.
     *
     * @param key the configuration key
     * @return list of trimmed values, empty list if key doesn't exist
     */
    public static List<String> strings(String key) {
        if (CACHE == null)
            throw new IllegalStateException("Call ZCfg.loadBaseConfig(appName) first");
        var value = CACHE.getProperty(key);
        if (value == null)
            return List.of();
        return split(value);
    }

    static List<String> split(String value) {
        var values = value.split(",");
        return Stream.of(values)
                .map(String::trim)
                .toList();
    }
}