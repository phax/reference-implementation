package com.ingroupe.efti.eftigate.dto.logstash;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogstashRegistryDto extends LogstashAllDto {
    public final String metadataId;
    public final String eFTIDataId;
    public final String interfaceType;
}
