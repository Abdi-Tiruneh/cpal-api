package com.commercepal.apiservice.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fails fast at bootstrap if required environment variables are missing or empty.
 * Runs before the application context is fully created so we get a single clear
 * error message instead of multiple placeholder resolution failures.
 */
public class RequiredEnvironmentValidator implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String[] REQUIRED_VARS = {
            "DB_URL",
            "DB_USERNAME",
            "DB_PASSWORD",
            "JWT_SECRET"
    };

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        List<String> missing = new ArrayList<>();
        for (String var : REQUIRED_VARS) {
            String value = env.getProperty(var);
            if (value == null || value.isBlank()) {
                missing.add(var);
            }
        }
        if (!missing.isEmpty()) {
            String message = String.format(
                    "Missing required environment variables: %s.",
                    String.join(", ", missing)
            );
            throw new IllegalStateException(message);
        }
    }
}
