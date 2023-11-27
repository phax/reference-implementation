package com.ingroupe.efti.eftigate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class RequestUuidDto {

    @NotNull
    private String requestUuid;

    @NotNull
    private String status;

    private String errorDescription;

    private byte[] eFTIData;

}
