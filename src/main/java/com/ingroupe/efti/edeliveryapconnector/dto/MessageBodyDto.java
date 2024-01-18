package com.ingroupe.efti.edeliveryapconnector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageBodyDto {
    private String requestUuid;
    private String eftidataUuid;
    private String status;
    private String errorDescription;
    @JsonProperty("eFTIData")
    private Object eFTIData;
}
