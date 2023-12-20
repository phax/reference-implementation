package com.ingroupe.efti.eftigate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformationDto {

    private int id;
    private String email;
    private String streetName;
    private String buildingNumber;
    private String city;
    private String additionalLine;
    private String postalCode;
}
