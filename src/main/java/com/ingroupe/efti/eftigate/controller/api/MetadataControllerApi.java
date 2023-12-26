package com.ingroupe.efti.eftigate.controller.api;

import com.ingroupe.efti.eftigate.config.security.Roles;
import com.ingroupe.efti.eftigate.dto.MetadataRequestDto;
import com.ingroupe.efti.eftigate.dto.MetadataResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Metadata controller" , description = "Interface to search by metadata")
@RequestMapping("/v1")
public interface MetadataControllerApi {

    @Operation(summary = "Send Search Request", description = "Send a search request to retreive an efti data by metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/getMetadata")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    MetadataResponseDto createRequest(final @RequestBody MetadataRequestDto metadataRequestDto);

}
