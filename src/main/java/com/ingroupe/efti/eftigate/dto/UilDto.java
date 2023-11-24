package com.ingroupe.efti.eftigate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UilDto {
    @NotNull
    private String gate;
    @NotNull
    private String uuid;
    @NotNull
    private String platform;
}
