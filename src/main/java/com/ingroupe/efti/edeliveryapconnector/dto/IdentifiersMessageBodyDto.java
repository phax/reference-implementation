package com.ingroupe.efti.edeliveryapconnector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentifiersMessageBodyDto {
    private String requestUuid;
    private String transportMode;
    @JsonProperty("vehicleId")
    private String vehicleID;
    private String vehicleCountry;
    private Boolean isDangerousGoods;
    @JsonProperty("eFTIGateIndicator")
    private List<String> eFTIGateIndicator;
}
