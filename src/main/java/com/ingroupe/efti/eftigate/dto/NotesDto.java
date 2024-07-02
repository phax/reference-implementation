package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.dto.AbstractUilDto;
import com.ingroupe.efti.commons.dto.ValidableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotesDto extends AbstractUilDto implements ValidableDto {
    private String requestUuid;
    private String note;
}
