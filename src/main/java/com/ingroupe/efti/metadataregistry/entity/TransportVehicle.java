package com.ingroupe.efti.metadataregistry.entity;

import com.ingroupe.efti.commons.enums.TransportMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transportvehicle")
public class TransportVehicle {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Enumerated
    private TransportMode transportMode;
    private int sequence;
    private String vehicleId;
    private String vehicleCountry;
    private LocalDateTime journeyStart;
    private String countryStart;
    private LocalDateTime journeyEnd;
    private String countryEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="metadata")
    MetadataEntity metadata;
}
