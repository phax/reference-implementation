package com.ingroupe.efti.commons.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetadataDto extends AbstractUilDto {

    private long id;
    private boolean isDangerousGoods;
    private String journeyStart;
    private String countryStart;
    private String journeyEnd;
    private String countryEnd;
    private String metadataUUID;
    @NotEmpty(message = "TRANSPORT_VEHICLES_MISSING")
    @Valid
    private List<TransportVehicleDto> transportVehicles;
    private boolean isDisabled;
}
