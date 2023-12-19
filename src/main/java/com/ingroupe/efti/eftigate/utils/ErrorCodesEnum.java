package com.ingroupe.efti.eftigate.utils;

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

    AP_SUBMISSION_ERROR("Error during ap submission."),
    REQUEST_BUILDING("Error while building request."),
    UUID_NOT_FOUND(" Uuid not found.");

    private final String message;
}
