package com.ingroupe.efti.commons.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCodesEnum {
    UIL_GATE_EMPTY("Gate should not be empty."),
    UIL_GATE_TOO_LONG("Gate max length is 255 characters."),
    UIL_GATE_INCORRECT_FORMAT("Gate format incorrect."),

    UIL_PLATFORM_EMPTY("Platform should not be empty."),
    UIL_PLATFORM_TOO_LONG("Platform max length is 255 characters."),
    UIL_PLATFORM_INCORRECT_FORMAT("Platform format incorrect."),

    UIL_UUID_EMPTY("Uuid should not be empty."),
    UIL_UUID_TOO_LONG("Uuid max length is 36 characters."),
    UIL_UUID_INCORRECT_FORMAT("Uuid format incorrect."),

    AUTHORITY_MISSING("Authority missing."),
    AUTHORITY_COUNTRY_MISSING("Authority country missing."),
    AUTHORITY_COUNTRY_TOO_LONG("Authority country too long."),
    AUTHORITY_COUNTRY_UNKNOWN("Authority country unknown."),
    AUTHORITY_LEGAL_CONTACT_MISSING("Authority legal contact missing."),
    AUTHORITY_WORKING_CONTACT_MISSING("Authority working contact missing."),
    AUTHORITY_IS_EMERGENCY_MISSING("Authority is emergency missing."),
    AUTHORITY_NAME_MISSING("Authority name missing."),
    AUTHORITY_NAME_TOO_LONG("Authority name too long."),
    AUTHORITY_NATIONAL_IDENTIFIER_MISSING("Authority national identifier missing."),
    AUTHORITY_NATIONAL_IDENTIFIER_TOO_LONG("Authority national identifier too long."),

    CONTACT_MAIL_EMPTY("Contact mail empty."),
    CONTACT_MAIL_INCORRECT_FORMAT("Contact mail incorrect."),
    CONTACT_MAIL_TOO_LONG("Contact mail too long."),
    CONTACT_STREET_NAME_EMPTY("Contact name empty."),
    CONTACT_STREET_NAME_TOO_LONG("Contact name too long."),
    CONTACT_BUILDING_NUMBER_EMPTY("Contact building number empty."),
    CONTACT_BUILDING_NUMBER_TOO_LONG("Contact building number too long."),
    CONTACT_CITY_EMPTY("Contact city empty."),
    CONTACT_CITY_TOO_LONG("Contact city too long."),
    CONTACT_ADDITIONAL_LINE_TOO_LONG("Contact additional line too long."),
    CONTACT_POSTAL_CODE_EMPTY("Contact postal code empty."),
    CONTACT_POSTAL_CODE_TOO_LONG("Contact postal code too long."),

    AP_SUBMISSION_ERROR("Error during ap submission."),
    REQUEST_BUILDING("Error while building request."),
    UUID_NOT_FOUND(" Uuid not found."),

    PLATFORM_ERROR("Platform error");

    private final String message;
}
