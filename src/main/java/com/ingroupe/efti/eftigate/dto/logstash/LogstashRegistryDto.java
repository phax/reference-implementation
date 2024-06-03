package com.ingroupe.efti.eftigate.dto.logstash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogstashRegistryDto extends LogstashAllDto {
    public String metadataId;
    public String eFTIDataId;
    public String interfaceType;
}
