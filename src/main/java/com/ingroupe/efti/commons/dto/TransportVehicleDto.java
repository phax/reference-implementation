package com.ingroupe.efti.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ingroupe.efti.commons.enums.TransportMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportVehicleDto {
    private long id;
    private TransportMode transportMode;
    private int sequence;
    private String vehicleId;
    private String vehicleCountry;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    private LocalDateTime journeyStart;
    private String countryStart;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'+'SSSS")
    private LocalDateTime journeyEnd;
    private String countryEnd;
}
