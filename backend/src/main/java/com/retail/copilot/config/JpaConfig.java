package com.retail.copilot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.retail.copilot.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Hibernate dialect and datasource are configured via application.yml.
    // PostgreSQL native enum types (order_status, message_role, stock_status)
    // are mapped using @Enumerated(EnumType.STRING) with columnDefinition
    // on each entity field to match the DB type name.
}
