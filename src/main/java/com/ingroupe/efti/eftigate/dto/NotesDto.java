package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.dto.AbstractUilDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotesDto extends AbstractUilDto {

    private String requestUuid;
    private String note;
}
