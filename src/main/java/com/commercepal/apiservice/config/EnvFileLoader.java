package com.commercepal.apiservice.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Loads KEY=VALUE pairs from a .env file in the working directory and sets them
 * as system properties so Spring's environment sees them. Only sets properties
 * that are not already defined (environment variables / existing system props win).
 * Runs before Spring starts so RequiredEnvironmentValidator can see the values.
 */
public final class EnvFileLoader {

    private static final String ENV_FILE = ".env";

    public static void loadIfPresent() {
        Path path = Paths.get(System.getProperty("user.dir", ".")).resolve(ENV_FILE);
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                // Remove optional surrounding quotes
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (key.isEmpty()) {
                    continue;
                }
                // Don't override existing env or system property
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (Exception e) {
            // Ignore; running without .env is valid (e.g. production with real env vars)
        }
    }
}
