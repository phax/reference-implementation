package com.ingroupe.efti.eftilogger.dto;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LogRegistryDto extends LogAllDto {
    public final String metadataId;
    public final String eFTIDataId;
    public final String interfaceType;
}
