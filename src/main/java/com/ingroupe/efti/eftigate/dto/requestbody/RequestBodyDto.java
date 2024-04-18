package com.ingroupe.efti.eftigate.dto.requestbody;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestBodyDto {
    @JsonProperty
    private String eFTIData;
    @JsonProperty("eFTIPlatformUrl")
    private String eFTIPlatformUrl;
    private String requestUuid;
    @JsonProperty("eFTIDataUuid")
    private String eFTIDataUuid;
    private List<String> subsetEU;
    private List<String> subsetMS;
    private AuthorityBodyDto authority;
}
