package com.ingroupe.efti.eftigate.dto.requestbody;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ingroupe.efti.commons.enums.CountryIndicator;
import com.ingroupe.efti.commons.validator.ValueOfEnum;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataRequestBodyDto {
    private String requestUuid;
    private String transportMode;
    @JsonProperty("vehicleId")
    private String vehicleID;
    private String vehicleCountry;
    private Boolean isDangerousGoods;
    @JsonProperty("eFTIGateIndicator")
    private List<@Valid @ValueOfEnum(enumClass = CountryIndicator.class, message = "GATE_INDICATOR_INCORRECT") String> eFTIGateIndicator;

}
