package com.ingroupe.efti.eftigate.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.ingroupe.efti")
public class EftiConfig {

    @Bean(name = "modelMapper")
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    @ConfigurationProperties(prefix = "gate")
    public GateProperties gateProperties() {
        return new GateProperties();
    }
}
