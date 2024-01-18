package com.ingroupe.efti.eftigate.dto.requestbody;

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
    private String requestUuid;
    private String eFTIDataUuid;
    private List<String> subsetEU;
    private List<String> subsetMS;
    private AuthorityBodyDto authority;
}
