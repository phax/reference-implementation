package com.ingroupe.efti.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EDeliveryAction {
    UPLOAD_METADATA("uploadMetadata"),
    GET_UIL("getUIL");

    private final String value;
}
