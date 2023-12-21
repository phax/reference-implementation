package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.utils.CountryIndicator;
import com.ingroupe.efti.eftigate.utils.TransportMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataRequestDto {

    private TransportMode transportMode;
    private String vehicleID;
    private String vehicleCountry;
    private String isDangerousGoods;
    private List<CountryIndicator> eFTIGateIndicator;
}
