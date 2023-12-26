package com.ingroupe.efti.eftigate.controller.api;

import com.ingroupe.efti.eftigate.config.security.Roles;
import com.ingroupe.efti.eftigate.dto.RequestUuidDto;
import com.ingroupe.efti.eftigate.dto.UilDto;
import com.ingroupe.efti.eftigate.entity.ControlEntity;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Request controller" , description = "Interface to manage dataset request")
@RequestMapping("/v1")
public interface ControlControllerApi {

    @Hidden
    @GetMapping("/{id}")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    ResponseEntity<ControlEntity> getById(@PathVariable long id);

    @Operation(summary = "Send a request", description = "Allow to send a request for a dataset based on uil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/requestUil")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    ResponseEntity<RequestUuidDto> requestUil(@RequestBody UilDto uilDto);

    @Operation(summary = "Get a request", description = "Get a request for a given request uuid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/requestUil")
    @Secured(Roles.ROLE_ROAD_CONTROLER)
    ResponseEntity<RequestUuidDto> getRequestUil(@Parameter String requestUuid);

}
