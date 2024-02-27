package com.ingroupe.efti.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportVehicleDto {
    @JsonIgnore
    private long id;
    private String transportMode;
    @Max(value = 999, message = "SEQUENCE_TOO_LONG")
    private int sequence;
    @NotNull(message = "VEHICLE_ID_MISSING")
    @Length(max = 17, message = "VEHICLE_ID_TOO_LONG")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "VEHICLE_ID_INCORRECT_FORMAT")
    private String vehicleId;
    private String vehicleCountry;
    private String journeyStart;
    private String countryStart;
    private String journeyEnd;
    private String countryEnd;
}
