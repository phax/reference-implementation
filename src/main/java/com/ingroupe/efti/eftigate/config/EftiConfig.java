package com.ingroupe.efti.eftigate.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EftiConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
