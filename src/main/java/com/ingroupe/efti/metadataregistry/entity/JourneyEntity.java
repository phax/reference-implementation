package com.ingroupe.efti.metadataregistry.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.model.AbstractModel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class JourneyEntity extends AbstractModel {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime journeyStart;

    @Enumerated(EnumType.STRING)
    private CountryIndicator countryStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime journeyEnd;

    @Enumerated(EnumType.STRING)
    private CountryIndicator countryEnd;
}
