package com.ingroupe.efti.commons.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataResultDto implements Serializable {
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
