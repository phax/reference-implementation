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
import java.time.temporal.ChronoUnit;

@Configuration
@ComponentScan(basePackages = "com.ingroupe.efti")
@Slf4j
public class EftiConfig {

    public static final String DB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS";

    @Bean(name = "modelMapper")
    public ModelMapper modelMapper() {
        final ModelMapper modelMapper = new ModelMapper();

        Provider<LocalDateTime> localDateProvider = new AbstractProvider<>() {
            @Override
            public LocalDateTime get() {
                return LocalDateTime.now();
            }
        };

        Converter<String, LocalDateTime> toLocalDateTime = new AbstractConverter<>() {
            @Override
            protected LocalDateTime convert(final String source) {
                DateTimeFormatter toFormat;
                if (source.contains("+")){
                    toFormat = DateTimeFormatter.ofPattern(ISO_8601_FORMAT);
                }
                else {
                    toFormat = new DateTimeFormatterBuilder()
                            .appendPattern(DB_DATE_FORMAT)
                            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 9, true)
                            .toFormatter();
                }
                try {
                    return LocalDateTime.parse(source, toFormat);
                } catch (DateTimeParseException e) {
                    log.error("invalid date format {}", source);
                    return null;
                }
            }
        };
        Converter<LocalDateTime, String> toString = new AbstractConverter<>() {
            @Override
            protected String convert(final LocalDateTime source) {
                if (source != null) {
                    return source.truncatedTo(ChronoUnit.SECONDS)
                            .toString()
                            .concat("+00:00");
                }
                return null;
            }
        };
        modelMapper.createTypeMap(String.class, LocalDateTime.class);
        modelMapper.addConverter(toLocalDateTime);
        modelMapper.addConverter(toString);
        modelMapper.getTypeMap(String.class, LocalDateTime.class).setProvider(localDateProvider);
        return modelMapper;
    }

    @Bean
    @ConfigurationProperties(prefix = "gate")
    public GateProperties gateProperties() {
        return new GateProperties();
    }
}
