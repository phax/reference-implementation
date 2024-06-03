package com.ingroupe.efti.eftigate.dto.logstash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogstashRequestDto extends LogstashAllDto {
    public String requestId;
    public String eFTIDataId;
    public String responseId;
    public String authorityNationalUniqueIdentifier;
    public String authorityName;
    public String officerId;
    public String subsetEURequested;
    public String subsetMSRequested;
}
