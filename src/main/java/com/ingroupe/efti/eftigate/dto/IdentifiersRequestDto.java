package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.eftigate.entity.MetadataResults;
import com.ingroupe.efti.eftigate.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdentifiersRequestDto extends RequestDto {
    private MetadataResults metadataResults;
    public IdentifiersRequestDto(final ControlDto controlDto) {
        super(controlDto);
        setRequestType(RequestType.IDENTIFIER);
    }
}
