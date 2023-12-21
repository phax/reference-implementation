package com.ingroupe.efti.eftigate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UilDto {

    private static final String REGEX_URI = "\\w+:(\\/?\\/?)[^\\s]+";

    @NotBlank(message = "UIL_GATE_EMPTY")
    @Size(max = 255, message = "UIL_GATE_TOO_LONG")
    @Pattern(regexp = REGEX_URI, message = "UIL_GATE_INCORRECT_FORMAT")
    private String gate;

    @NotBlank(message = "UIL_UUID_EMPTY")
    @Size(max = 36, message = "UIL_UUID_TOO_LONG")
    @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}", message = "UIL_UUID_INCORRECT_FORMAT")
    private String uuid;

    @NotBlank(message = "UIL_PLATFORM_EMPTY")
    @Size(max = 255, message = "UIL_PLATFORM_TOO_LONG")
    @Pattern(regexp = REGEX_URI, message = "UIL_PLATFORM_INCORRECT_FORMAT")
    private String platform;

    @NotNull(message= "AUTHORITY_ERROR")
    private AuthorityDto authority;
}
