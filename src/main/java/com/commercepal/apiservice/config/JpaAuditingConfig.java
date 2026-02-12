package com.commercepal.apiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration to enable JPA auditing for BaseEntity. This enables automatic population of
 * createdAt and updatedAt fields.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}

