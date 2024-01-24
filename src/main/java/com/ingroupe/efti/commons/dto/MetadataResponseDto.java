package com.ingroupe.efti.commons.dto;

import com.ingroupe.efti.commons.enums.CountryIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataResponseDto {
    private CountryIndicator eFTIGate;
    private String requestUuid;
    private String status;
    private String errorDescription;
    private List<Object> metadata;
}
