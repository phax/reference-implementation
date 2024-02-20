package com.ingroupe.efti.eftigate.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.AbstractProvider;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

@Configuration
@ComponentScan(basePackages = "com.ingroupe.efti")
@Slf4j
public class EftiConfig {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Bean(name = "modelMapper")
    public ModelMapper modelMapper() {
        final ModelMapper mapper = new ModelMapper();

        Provider<LocalDateTime> localDateProvider = new AbstractProvider<>() {
            @Override
            public LocalDateTime get() {
                return LocalDateTime.now();
            }
        };

        Converter<String, LocalDateTime> toStringDate = new AbstractConverter<>() {
            @Override
            protected LocalDateTime convert(final String source) {
                DateTimeFormatter toFormatter = new DateTimeFormatterBuilder()
                        .appendPattern(DATE_FORMAT)
                        .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 9, true)
                        .toFormatter();
                try {
                    return LocalDateTime.parse(source, toFormatter);
                } catch (DateTimeParseException e) {
                    log.error("invalid date format {}", source);
                    return null;
                }
            }
        };

        mapper.createTypeMap(String.class, LocalDateTime.class);
        mapper.addConverter(toStringDate);
        mapper.getTypeMap(String.class, LocalDateTime.class).setProvider(localDateProvider);

        return mapper;
    }

    @Bean
    @ConfigurationProperties(prefix = "gate")
    public GateProperties gateProperties() {
        return new GateProperties();
    }
}
