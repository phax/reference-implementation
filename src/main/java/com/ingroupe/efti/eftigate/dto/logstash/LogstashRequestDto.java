package com.ingroupe.efti.eftigate.dto.logstash;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogstashRequestDto extends LogstashAllDto {
    public final String requestId;
    public final String eFTIDataId;
    public final String responseId;
    public final String authorityNationalUniqueIdentifier;
    public final String authorityName;
    public final String officerId;
    public final String subsetEURequested;
    public final String subsetMSRequested;
    public final String requestType;
}
