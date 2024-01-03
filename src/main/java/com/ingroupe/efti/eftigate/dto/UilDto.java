package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.dto.AbstractUilDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class UilDto extends AbstractUilDto {

    @Valid
    @NotNull(message= "AUTHORITY_MISSING")
    private AuthorityDto authority;
}
