package com.ingroupe.efti.eftigate.dto;

import com.ingroupe.efti.commons.enums.ErrorCodesEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private int id;
    private String errorCode;
    private String errorDescription;

    public static ErrorDto fromErrorCode(final ErrorCodesEnum errorCode) {
        return ErrorDto.builder()
                .errorCode(errorCode.name())
                .errorDescription(errorCode.getMessage()).build();
    }
}
