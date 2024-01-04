package com.ingroupe.efti.commons.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "CONTACT_MAIL_EMPTY")
    @Email(message = "CONTACT_MAIL_INCORRECT_FORMAT")
    @Size(max = 255, message = "CONTACT_EMAIL_TOO_LONG")
    private String email;
    @NotBlank(message = "CONTACT_STREET_NAME_EMPTY")
    @Size(max = 300, message = "CONTACT_STREET_NAME_TOO_LONG")
    private String streetName;
    @NotBlank(message = "CONTACT_BUILDING_NUMBER_EMPTY")
    @Size(max = 50, message = "CONTACT_BUILDING_NUMBER_TOO_LONG")
    private String buildingNumber;
    @NotBlank(message = "CONTACT_CITY_EMPTY")
    @Size(max = 100, message = "CONTACT_CITY_TOO_LONG")
    private String city;
    @Size(max = 300, message = "CONTACT_ADDITIONAL_LINE_TOO_LONG")
    private String additionalLine;
    @NotBlank(message = "CONTACT_POSTAL_CODE_EMPTY")
    @Size(max = 50, message = "CONTACT_POSTAL_CODE_TOO_LONG")
    private String postalCode;
}
