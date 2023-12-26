package com.ingroupe.efti.eftigate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotesDto {
    private String requestUuid;
    private String eFTIPlatformUrl;
    private String eFTIGateUrl;
    private String eFTIDataUuid;
    private String note;
}
