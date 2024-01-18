package com.ingroupe.platform.platformgatesimulator.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan("com.ingroupe.platform")
@ComponentScan(basePackages = {"com.ingroupe.platform", "com.ingroupe.efti"})
public class PlatformConfig {

    @Bean
    @ConfigurationProperties(prefix = "gate")
    public GateProperties gateProperties() {
        return new GateProperties();
    }
}
