package com.facepay.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Загрузчик конфигурации из application.yml с поддержкой env-переменных.
 * <p>
 * Приоритет чтения свойств:
 * 1. Системные свойства (-D)
 * 2. Переменные окружения (System.getenv)
 * 3. Значения по умолчанию из application.yml
 * </p>
 */
public class ConfigLoader implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient Map<String, String> properties;

    public ConfigLoader() {
        this.properties = loadProperties();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            if (is == null) {
                throw new IllegalStateException("application.yml not found on classpath");
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> root = mapper.readValue(is, Map.class);
            return flatten(root, "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> flatten(Map<String, Object> map, String prefix) {
        Map<String, String> result = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                result.putAll(flatten((Map<String, Object>) entry.getValue(), key));
            } else {
                String value = entry.getValue().toString();
                if (value.startsWith("${") && value.endsWith("}")) {
                    String[] parts = value.substring(2, value.length() - 1).split(":", 2);
                    String envName = parts[0];
                    String defaultValue = parts.length > 1 ? parts[1] : "";

                    String resolved = System.getProperty(envName);
                    if (resolved == null || resolved.isBlank()) {
                        resolved = System.getenv(envName);
                    }
                    if (resolved == null || resolved.isBlank()) {
                        resolved = defaultValue;
                    }
                    value = resolved;
                }
                result.put(key, value);
            }
        }
        return result;
    }

    public String getString(String key) {
        return Objects.requireNonNull(properties.get(key), "Property not found: " + key);
    }

    public String getString(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Восстанавливает состояние после десериализации.
     * Вызывается Spark-ом при десериализации на executor'е.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.properties = loadProperties();
    }
}
