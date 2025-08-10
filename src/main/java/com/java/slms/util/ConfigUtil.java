package com.java.slms.util;

import java.io.*;
import java.util.*;

public class ConfigUtil
{

    private static final String ENV_SLMS_CONFIG = "SLMS_CONFIG";
    private static final Map<String, String> properties = new HashMap<>();

    // This will be initialized once when class is loaded
    static
    {
        String configPath = System.getenv(ENV_SLMS_CONFIG);
        if (configPath == null || configPath.isEmpty())
        {
            throw new RuntimeException("Environment variable " + ENV_SLMS_CONFIG + " not set");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2)
                {
                    properties.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error reading config file at: " + configPath, e);
        }
    }

    // Retrieve a required key, or throw
    public static String getRequired(String key)
    {
        String value = properties.get(key);
        if (value == null || value.isEmpty())
        {
            throw new RuntimeException("Missing required config key: " + key);
        }
        return value;
    }

    // Retrieve an optional key (null if not found)
    public static String getOptional(String key)
    {
        return properties.get(key);
    }

    // All properties if needed
    public static Map<String, String> getAll()
    {
        return Collections.unmodifiableMap(properties);
    }
}
