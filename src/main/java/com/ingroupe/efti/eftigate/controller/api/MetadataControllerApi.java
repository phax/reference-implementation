package com.ingroupe.efti.eftigate.controller.api;

import com.ingroupe.efti.commons.dto.MetadataRequestDto;
import com.ingroupe.efti.eftigate.config.security.Roles;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @PostMapping("/getMetadata")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    ResponseEntity<RequestUuidDto> getMetadata(final @RequestBody MetadataRequestDto metadataRequestDto);

}
