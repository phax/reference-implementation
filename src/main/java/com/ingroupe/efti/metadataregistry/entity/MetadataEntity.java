package com.ingroupe.efti.metadataregistry.entity;

import com.ingroupe.efti.commons.enums.CountryIndicator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "metadata")
public class MetadataEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    private String eFTIPlatformUrl;
    private String eFTIDataUuid;
    private String eFTIGateUrl;
    private boolean isDangerousGoods;
    private LocalDateTime journeyStart;
    @Enumerated(EnumType.STRING)
    private CountryIndicator countryStart;
    private LocalDateTime journeyEnd;
    @Enumerated(EnumType.STRING)
    private CountryIndicator countryEnd;
    private String metadataUUID;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<TransportVehicle> transportVehicles;
}
