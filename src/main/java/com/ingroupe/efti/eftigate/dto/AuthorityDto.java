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
    private String email;
    private String physicalAddress;
    private boolean isEmergencyService;
    private String authorityName;
    private String nationalUniqueIdentifier;
}
