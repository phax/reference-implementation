package com.ingroupe.efti.metadataregistry.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.enums.TransportMode;
import com.ingroupe.efti.commons.model.AbstractModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transportvehicle")
public class TransportVehicle extends AbstractModel implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Enumerated(EnumType.STRING)
    private TransportMode transportMode;
    private int sequence;
    private String vehicleId;
    @Enumerated(EnumType.STRING)
    private CountryIndicator vehicleCountry;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime journeyStart;
    @Enumerated(EnumType.STRING)
    private CountryIndicator countryStart;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime journeyEnd;
    @Enumerated(EnumType.STRING)
    private CountryIndicator countryEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="metadata")
    MetadataEntity metadata;
}
