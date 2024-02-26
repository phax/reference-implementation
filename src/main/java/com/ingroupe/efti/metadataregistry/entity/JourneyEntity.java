package com.ingroupe.efti.metadataregistry.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.model.AbstractModel;
import com.ingroupe.efti.metadataregistry.utils.OffsetDateTimeDeserializer;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@MappedSuperclass
@Data
public abstract class JourneyEntity extends AbstractModel {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss XXX")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime journeyStart;

    @Enumerated(EnumType.STRING)
    private CountryIndicator countryStart;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime journeyEnd;

    @Enumerated(EnumType.STRING)
    private CountryIndicator countryEnd;
}
