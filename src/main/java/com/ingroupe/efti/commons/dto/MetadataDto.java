package com.ingroupe.efti.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDto {
    private long id;
    @JsonProperty("eFTIPlatformUrl")
    private String eFTIPlatformUrl;
    @JsonProperty("eFTIDataUuid")
    private String eFTIDataUuid;
    @JsonProperty("eFTIGateUrl")
    private String eFTIGateUrl;
    private boolean isDangerousGoods;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    private LocalDateTime journeyStart;
    private String countryStart;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    private LocalDateTime journeyEnd;
    private String countryEnd;
    private String metadataUUID;
    private List<TransportVehicleDto> transportVehicles;
}
