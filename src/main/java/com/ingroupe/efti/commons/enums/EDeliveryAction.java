package com.ingroupe.efti.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EDeliveryAction {
    UPLOAD_METADATA("uploadMetadata"),
    GET_UIL("getUIL");

    private final String value;

    public static EDeliveryAction getFromValue(final String value) {
        for (final EDeliveryAction action : EDeliveryAction.values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        return null;
    }
}
