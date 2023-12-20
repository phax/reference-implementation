package com.ingroupe.efti.eftigate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityDto {
    private int id;
    private String country;
    private ContactInformationDto legalContact;
    private ContactInformationDto workingContact;
    private boolean isEmergencyService;
    private String name;
    private String nationalUniqueIdentifier;
}
