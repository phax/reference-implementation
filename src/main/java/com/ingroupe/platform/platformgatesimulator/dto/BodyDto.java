package com.ingroupe.platform.platformgatesimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyDto {
    private String eFTIData;
    private String status;
    private String requestUuid;
}
